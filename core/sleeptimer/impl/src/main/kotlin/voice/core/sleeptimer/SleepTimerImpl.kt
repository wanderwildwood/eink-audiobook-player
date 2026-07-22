package voice.core.sleeptimer

import androidx.datastore.core.DataStore
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
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
import kotlin.math.max
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
          startCountdown(pref.duration)
        }
      }
    }
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
   * late shake before giving up.
   */
  private tailrec suspend fun startCountdown(duration: Duration) {
    Logger.d("startCountdown(duration=$duration)")
    val shookDuringCountdown = coroutineScope {
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

    if (shookDuringCountdown) {
      Logger.i("Shake detected, resetting timer")
      startCountdown(duration)
      return
    }

    playerController.setVolume(1f)
    state.value = SleepTimerState.Disabled

    val fadeOutDuration = fadeOutStore.data.first()
    playerController.pauseWithRewind(fadeOutDuration)

    val shookDuringGrace = withTimeoutOrNull(SHAKE_TO_RESET_TIME) {
      shakeDetector.detect()
      true
    } ?: false
    playerController.setVolume(1F)
    if (shookDuringGrace) {
      Logger.i("Shake detected, resetting timer")
      playerController.play()
      startCountdown(duration)
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
  ): Boolean {
    var left = duration
    state.value = SleepTimerState.Enabled.WithDuration(left)
    playerController.setVolume(1F)
    val fadeOutDuration = fadeOutStore.data.first()

    while (left > Duration.ZERO) {
      suspendUntilPlaying()
      val interval = if (left < fadeOutDuration) 200.milliseconds else 500.milliseconds
      if (left < fadeOutDuration) {
        updateVolume(left, fadeOutDuration)
      }
      val shook = withTimeoutOrNull(interval) {
        shakeSignal.receive()
        true
      } ?: false
      if (shook) {
        playerController.setVolume(1F)
        return true
      }
      left = max((left - interval).inWholeMilliseconds, 0).milliseconds
      state.value = SleepTimerState.Enabled.WithDuration(left)
    }
    return false
  }

  private fun updateVolume(
    left: Duration,
    fadeOutDuration: Duration,
  ) {
    val percentage = (left / fadeOutDuration).toFloat().coerceIn(0f, 1f)
    val volume = 1 - FastOutSlowInInterpolator().getInterpolation(1 - percentage)
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
  }
}
