package voice.core.sleeptimer

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.FadeOutStore
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.logging.api.Logger
import voice.core.playback.PlayerController
import voice.core.playback.playstate.PlayStateManager
import voice.core.playback.playstate.PlayStateManager.PlayState.Playing
import java.time.Clock
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SleepTimerImpl internal constructor(
  private val playStateManager: PlayStateManager,
  private val shakeDetector: ShakeDetector,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  private val playerController: PlayerController,
  @FadeOutStore
  private val fadeOutStore: DataStore<Duration>,
  dispatcherProvider: DispatcherProvider,
  private val tracker: SleepTimerTracker,
  private val shakeChime: ShakeChime,
  private val clock: Clock,
) : SleepTimer {

  private val scope = MainScope(dispatcherProvider)
  override val state: StateFlow<SleepTimerState>
    field = MutableStateFlow<SleepTimerState>(SleepTimerState.Disabled)

  private var job: Job? = null

  override fun enable(mode: SleepTimerMode) {
    tracker.enabled(mode)
    disable() // cancel any active job first

    job = scope.launch {
      when (mode) {
        is SleepTimerMode.TimedWithDuration -> startCountdown(mode.duration)
        SleepTimerMode.TimedWithDefault -> {
          val pref = sleepTimerPreferenceStore.data.first()
          if (pref.endOfChapterEnabled) {
            waitForChapterEnd()
          } else {
            startCountdown(pref.duration)
          }
        }
      }
    }
  }

  /**
   * Waits for the current chapter to end (the playlist advancing to a different chapter id, or
   * playback stopping) and pauses right there - no shake-to-extend here, since there's no
   * countdown to reset, unlike [startCountdown].
   */
  private suspend fun waitForChapterEnd() {
    val initialChapterId = playerController.livePlaybackStateFlow().first()?.chapterId ?: return
    state.value = SleepTimerState.Enabled.UntilChapterEnd

    val newState = playerController.livePlaybackStateFlow()
      .first { it == null || it.chapterId != initialChapterId }

    state.value = SleepTimerState.Disabled
    playerController.pause()
    newState?.chapterId?.let { playerController.setPosition(0L, it) }
  }

  override fun disable() {
    tracker.disabled()
    job?.cancel()
    job = null
    state.value = SleepTimerState.Disabled
    playerController.setVolume(1F)
  }

  /**
   * Runs the countdown, listening for a shake the whole time (not just after expiry) - a shake
   * at any point resets the countdown back to [duration] and keeps it running. Only if it
   * expires with no shake does playback actually pause, with one more grace window to catch a
   * late shake before giving up. Only a shake during the fade-out chimes; see below.
   */
  private tailrec suspend fun startCountdown(duration: Duration) {
    Logger.d("startCountdown(duration=$duration)")
    val countdownEnd = coroutineScope {
      val shakeSignal = Channel<Unit>(Channel.CONFLATED)
      val shakeJob = launch {
        while (isActive) {
          shakeDetector.detect()
          shakeSignal.trySend(Unit)
        }
      }
      try {
        tickDownOrUntilShake(duration, shakeSignal)
      } finally {
        shakeJob.cancel()
      }
    }

    if (countdownEnd != CountdownEnd.Expired) {
      Logger.i("Shake detected, resetting timer")
      // Only while the volume is on its way down. A shake resets the timer at any point in the
      // countdown, and most of those are just someone moving in bed with the book still playing
      // normally - chiming for each one turns a confirmation into a nuisance. Once the fade has
      // started the book is audibly winding down, which is the moment it's worth being told that
      // the shake registered.
      if (countdownEnd == CountdownEnd.ShookWhileFading) {
        shakeChime.play()
      }
      startCountdown(duration)
      return
    }

    playerController.setVolume(1f)
    state.value = SleepTimerState.Disabled

    val fadeOutDuration = fadeOutStore.data.first()
    playerController.pauseWithRewind(fadeOutDuration)

    val pausedAtMillis = clock.millis()
    val shookDuringGrace = withTimeoutOrNull(SHAKE_TO_RESET_TIME) {
      shakeDetector.detect()
      true
    } ?: false
    playerController.setVolume(1F)
    // The timeout above is not enough on its own to bound this window. Coroutine delays run on a
    // monotonic clock that does not advance while the device is suspended, and a non-wakeup
    // sensor delivers nothing during suspend either - so once the screen goes off and the device
    // sleeps, this window stops elapsing and simply resumes hours later. The first movement after
    // that, picking the phone up to check the time in the middle of the night, arrived as a shake
    // inside a window that should have closed long ago, and the book started playing again.
    // [clock] is wall-clock time, which does include time spent suspended.
    val elapsed = (clock.millis() - pausedAtMillis).milliseconds
    if (shookDuringGrace && elapsed <= SHAKE_TO_RESET_TIME) {
      Logger.i("Shake detected, resetting timer")
      playerController.play()
      startCountdown(duration)
    } else if (shookDuringGrace) {
      Logger.i("Shake ignored, $elapsed elapsed since playback was paused")
    }
  }

  /**
   * Ticks [duration] down to zero, fading the volume out over the last [FadeOutStore] duration.
   * Returns true the moment a shake is received (caller should restart the countdown), false if
   * it ran out naturally.
   */
  private suspend fun tickDownOrUntilShake(
    duration: Duration,
    shakeSignal: ReceiveChannel<Unit>,
  ): CountdownEnd {
    var left = duration
    state.value = SleepTimerState.Enabled.WithDuration(left)
    playerController.setVolume(1F)
    val fadeOutDuration = fadeOutStore.data.first()

    while (left > Duration.ZERO) {
      suspendUntilPlaying()
      val fading = left < fadeOutDuration
      val interval = if (fading) 200.milliseconds else 500.milliseconds
      if (fading) {
        updateVolume(left, fadeOutDuration)
      }
      val shook = withTimeoutOrNull(interval) {
        shakeSignal.receive()
        true
      } ?: false
      if (shook) {
        playerController.setVolume(1F)
        return if (fading) CountdownEnd.ShookWhileFading else CountdownEnd.ShookWhileCounting
      }
      left = max((left - interval).inWholeMilliseconds, 0).milliseconds
      state.value = SleepTimerState.Enabled.WithDuration(left)
    }
    return CountdownEnd.Expired
  }

  /** How a countdown finished, which is what decides whether the shake is worth a chime. */
  private enum class CountdownEnd {
    Expired,
    ShookWhileCounting,
    ShookWhileFading,
  }

  /**
   * Fades evenly in decibels, which is how the ear hears loudness - a steady wind-down rather
   * than a lurch.
   *
   * The interpolated curve this replaces was front-loaded: it gave up only 2dB in the first
   * quarter of the fade and then fell off, passing -13dB by the halfway point and being
   * inaudible for the last third. That made the fade feel abrupt, and left most of its length
   * as silence you could not hear well enough to react to. Same duration, spread evenly, is
   * about 1.5dB per second across a 30 second fade.
   */
  private fun updateVolume(
    left: Duration,
    fadeOutDuration: Duration,
  ) {
    val remaining = (left / fadeOutDuration).toFloat().coerceIn(0f, 1f)
    val volume = if (remaining <= 0f) {
      0f
    } else {
      10.0.pow(-FADE_OUT_RANGE_DB * (1f - remaining) / 20.0).toFloat()
    }
    playerController.setVolume(volume)
  }

  private suspend fun suspendUntilPlaying() {
    if (playStateManager.playState != Playing) {
      Logger.i("Not playing. Waiting for playback to continue.")
      playStateManager.playStateFlow.first { it == Playing }
      Logger.i("Playback resumed.")
    }
  }

  internal companion object {
    val SHAKE_TO_RESET_TIME = 30.seconds

    /** How far the fade travels before playback pauses. -45dB is inaudible in a quiet room. */
    const val FADE_OUT_RANGE_DB = 45.0
  }
}
