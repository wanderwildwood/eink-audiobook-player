package audiobook.core.sleeptimer.impl

import audiobook.core.common.DispatcherProvider
import audiobook.core.data.BookId
import audiobook.core.data.ChapterId
import audiobook.core.data.sleeptimer.SleepTimerPreference
import audiobook.core.logging.api.LogWriter
import audiobook.core.logging.api.Logger
import audiobook.core.playback.LivePlaybackState
import audiobook.core.playback.PlayerController
import audiobook.core.playback.playstate.PlayStateManager
import audiobook.core.sleeptimer.ManualClock
import audiobook.core.sleeptimer.ShakeChime
import audiobook.core.sleeptimer.ShakeDetector
import audiobook.core.sleeptimer.SleepTimer
import audiobook.core.sleeptimer.SleepTimerImpl
import audiobook.core.sleeptimer.SleepTimerMode
import audiobook.core.sleeptimer.SleepTimerState
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.BeforeClass
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private class TestShakeChime : ShakeChime {
  var plays = 0
    private set

  override fun play() {
    plays++
  }
}

private class TestShakeDetector : ShakeDetector {
  private val shakes = Channel<Unit>(capacity = Channel.UNLIMITED)
  override suspend fun detect() {
    shakes.receive()
  }

  fun shake() {
    shakes.trySend(Unit)
  }
}

class SleepTimerImplTest {

  private val playStateManager = PlayStateManager().apply {
    playState = PlayStateManager.PlayState.Playing
  }
  private val shakeDetector = TestShakeDetector()
  private val sleepTimerPreferenceStore = MemoryDataStore(SleepTimerPreference.Default)
  private val setVolumeSlots = mutableListOf<Float>()
  private val playerController = mockk<PlayerController> {
    every { setVolume(capture(setVolumeSlots)) } just Runs
    every { pauseWithRewind(any()) } answers {
      playStateManager.playState = PlayStateManager.PlayState.Paused
    }
    every {
      play()
    } answers {
      playStateManager.playState = PlayStateManager.PlayState.Playing
    }
  }

  private val fadeOutStore = MemoryDataStore(2.seconds)
  private val shakeChime = TestShakeChime()

  // Wall-clock time, which unlike the test scheduler's virtual time is what the grace window is
  // measured against. Tests that care advance it explicitly.
  private val clock = ManualClock(Instant.parse("2026-08-19T22:00:00Z"))
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  private val sleepTimer: SleepTimer

  init {
    val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)
    sleepTimer = SleepTimerImpl(
      playStateManager,
      shakeDetector,
      sleepTimerPreferenceStore,
      playerController,
      fadeOutStore,
      dispatcherProvider,
      tracker = mockk(relaxed = true),
      shakeChime = shakeChime,
      clock = clock,
    )
  }

  @Test
  fun `initial state is disabled`() {
    assertEquals(expected = SleepTimerState.Disabled, actual = sleepTimer.state.value)
  }

  @Test
  fun `enable with fixed duration eventually disables and pauses playback`() = testScope.runTest {
    sleepTimer.enable(SleepTimerMode.TimedWithDuration(1.seconds))

    advanceTimeBy(2.seconds)
    assertEquals(expected = SleepTimerState.Disabled, actual = sleepTimer.state.value)
    coVerify(exactly = 1) { playerController.pauseWithRewind(any()) }
  }

  @Test
  fun `disable cancels timer and resets state`() = testScope.runTest {
    sleepTimer.enable(SleepTimerMode.TimedWithDuration(5.seconds))
    advanceTimeBy(1.seconds)

    sleepTimer.disable()

    assertEquals(expected = SleepTimerState.Disabled, actual = sleepTimer.state.value)
  }

  @Test
  fun withDurationResetsVolume() = testScope.runTest {
    sleepTimer.enable(SleepTimerMode.TimedWithDuration(5.seconds))
    advanceTimeBy(3.seconds)
    yield()

    // after the first 3 seconds, the volume should not have been decreased
    assertEquals(expected = setOf(1F), actual = setVolumeSlots.toSet())

    setVolumeSlots.clear()
    advanceTimeBy(1.seconds)
    yield()
    // now we're in fade-out phase, volume should decrease
    assertTrue(setVolumeSlots.isNotEmpty())
    assertTrue(setVolumeSlots.zipWithNext().all { (previous, next) -> previous > next })

    // after the timer finished, volume should be reset
    setVolumeSlots.clear()
    advanceTimeBy(2.seconds)
    yield()
    assertEquals(expected = 1f, actual = setVolumeSlots.last())
  }

  @Test
  fun shake_does_not_cancel_second_countdown_after_window() = testScope.runTest {
    // Use a LONG duration so we can observe behavior across the 30s window
    val longDuration = SleepTimerImpl.SHAKE_TO_RESET_TIME * 2

    sleepTimer.enable(SleepTimerMode.TimedWithDuration(longDuration))

    // 1) Let the first countdown finish and enter the shake window
    advanceTimeBy(longDuration + 1.seconds)
    runCurrent()
    coVerify(exactly = 1) { playerController.pauseWithRewind(any()) }
    assertEquals(expected = SleepTimerState.Disabled, actual = sleepTimer.state.value)

    // 2) Trigger the shake → a new countdown should start independently of the old timeout
    shakeDetector.shake()
    runCurrent()
    verify(exactly = 1) { playerController.play() }
    assertEquals(expected = SleepTimerState.Enabled.WithDuration(longDuration), actual = sleepTimer.state.value)

    // 3) Advance past the original 30s shake window and allow the second countdown to finish
    advanceTimeBy(SleepTimerImpl.SHAKE_TO_RESET_TIME + longDuration + 2.seconds)
    runCurrent()

    // The second countdown should complete normally
    coVerify(exactly = 2) { playerController.pauseWithRewind(any()) }
    assertEquals(expected = SleepTimerState.Disabled, actual = sleepTimer.state.value)
  }

  @Test
  fun shake_after_grace_window_elapsed_in_wall_clock_does_not_resume() = testScope.runTest {
    val duration = 1.minutes
    sleepTimer.enable(SleepTimerMode.TimedWithDuration(duration))

    advanceTimeBy(duration + 1.seconds)
    runCurrent()
    coVerify(exactly = 1) { playerController.pauseWithRewind(any()) }

    // The device suspends here. Virtual time does not move, exactly as the coroutine timeout and
    // the non-wakeup sensor both stall while the CPU is asleep - but hours of real time pass.
    clock.instant = clock.instant.plusSeconds(4 * 60 * 60)

    // Picking the phone up to check the time registers as a shake once the device wakes.
    shakeDetector.shake()
    runCurrent()

    verify(exactly = 0) { playerController.play() }
    assertEquals(expected = 0, actual = shakeChime.plays)
    assertEquals(expected = SleepTimerState.Disabled, actual = sleepTimer.state.value)
  }

  @Test
  fun shake_within_grace_window_resumes_without_chiming() = testScope.runTest {
    val duration = 1.minutes
    sleepTimer.enable(SleepTimerMode.TimedWithDuration(duration))

    advanceTimeBy(duration + 1.seconds)
    runCurrent()

    clock.instant = clock.instant.plusSeconds(5)
    shakeDetector.shake()
    runCurrent()

    verify(exactly = 1) { playerController.play() }
    // Playback starting again is its own confirmation - the chime is for the fade, when the book
    // is still playing and a reset would otherwise be silent.
    assertEquals(expected = 0, actual = shakeChime.plays)
    assertEquals(expected = SleepTimerState.Enabled.WithDuration(duration), actual = sleepTimer.state.value)
  }

  @Test
  fun shake_mid_countdown_does_not_chime() = testScope.runTest {
    sleepTimer.enable(SleepTimerMode.TimedWithDuration(5.minutes))
    advanceTimeBy(2.minutes)
    runCurrent()

    shakeDetector.shake()
    runCurrent()

    // Rolling over in bed with the book still playing normally. It resets the timer, quietly.
    assertEquals(expected = 0, actual = shakeChime.plays)
  }

  @Test
  fun shake_while_volume_is_fading_chimes() = testScope.runTest {
    val duration = 1.minutes
    sleepTimer.enable(SleepTimerMode.TimedWithDuration(duration))

    // fadeOutStore is 2s here, so this lands one second into the fade.
    advanceTimeBy(duration - 1.seconds)
    runCurrent()

    shakeDetector.shake()
    runCurrent()

    assertEquals(expected = 1, actual = shakeChime.plays)
    assertEquals(expected = SleepTimerState.Enabled.WithDuration(duration), actual = sleepTimer.state.value)
  }

  @Test
  fun shake_mid_countdown_resets_timer_without_pausing() = testScope.runTest {
    val duration = 5.minutes

    sleepTimer.enable(SleepTimerMode.TimedWithDuration(duration))

    // Let it tick down partway, well short of expiry.
    advanceTimeBy(2.minutes)
    runCurrent()
    assertTrue(sleepTimer.state.value is SleepTimerState.Enabled.WithDuration)
    assertTrue((sleepTimer.state.value as SleepTimerState.Enabled.WithDuration).leftDuration < duration)

    // A shake mid-countdown should reset it back to the full duration - no pause, no grace window.
    shakeDetector.shake()
    runCurrent()

    coVerify(exactly = 0) { playerController.pauseWithRewind(any()) }
    assertEquals(expected = SleepTimerState.Enabled.WithDuration(duration), actual = sleepTimer.state.value)
  }

  @Test
  fun `end of chapter mode stops at the next chapter and rewinds to its start`() = testScope.runTest {
    val bookId = BookId("book")
    val chapterOne = ChapterId("chapter-1")
    val chapterTwo = ChapterId("chapter-2")
    val livePlaybackFlow = MutableStateFlow(
      LivePlaybackState(bookId, chapterOne, positionMs = 0L, isPlaying = true, playbackSpeed = 1f),
    )
    every { playerController.livePlaybackStateFlow() } returns livePlaybackFlow
    every { playerController.pause() } just Runs
    every { playerController.setPosition(any(), any()) } just Runs

    sleepTimerPreferenceStore.updateData { it.copy(endOfChapterEnabled = true) }
    sleepTimer.enable(SleepTimerMode.TimedWithDefault)
    runCurrent()

    assertEquals(expected = SleepTimerState.Enabled.UntilChapterEnd, actual = sleepTimer.state.value)
    coVerify(exactly = 0) { playerController.pause() }

    livePlaybackFlow.value = LivePlaybackState(bookId, chapterTwo, positionMs = 0L, isPlaying = true, playbackSpeed = 1f)
    runCurrent()

    assertEquals(expected = SleepTimerState.Disabled, actual = sleepTimer.state.value)
    coVerify(exactly = 1) { playerController.pause() }
    verify(exactly = 1) { playerController.setPosition(0L, chapterTwo) }
  }

  companion object {

    @BeforeClass
    @JvmStatic
    fun setup() {
      Logger.install(
        object : LogWriter {
          override fun log(
            severity: Logger.Severity,
            message: String,
            throwable: Throwable?,
          ) {
            println(
              buildString {
                append("${severity.name}: ")
                append(message)
                if (throwable != null) {
                  append(", $throwable")
                }
              },
            )
          }
        },
      )
    }
  }
}
