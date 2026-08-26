package voice.core.sleeptimer

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.playback.playstate.PlayStateManager
import voice.core.sleeptimer.SleepTimerMode.TimedWithDuration
import voice.core.sleeptimer.impl.MemoryDataStore
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ReArmSleepTimerTest {
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)
  private val sleepTimerPreferenceStore = MemoryDataStore(SleepTimerPreference.Default)
  private val playStateManager = PlayStateManager()
  private val sleepTimer = mockk<SleepTimer> {
    val stateFlow = MutableStateFlow<SleepTimerState>(SleepTimerState.Disabled)
    every {
      state
    } returns stateFlow
    every {
      enable(any())
    } answers {
      stateFlow.value = when (val mode = firstArg<SleepTimerMode>()) {
        is TimedWithDuration -> SleepTimerState.Enabled.WithDuration(mode.duration)
        SleepTimerMode.TimedWithDefault -> SleepTimerState.Enabled.WithDuration(5.seconds)
      }
    }
  }

  private fun prefs(
    duration: Duration = 30.minutes,
    enabledLastSession: Boolean = false,
  ) = SleepTimerPreference(
    duration = duration,
    enabledLastSession = enabledLastSession,
  )

  private val sut = ReArmSleepTimer(
    sleepTimerPreferenceStore = sleepTimerPreferenceStore,
    playStateManager = playStateManager,
    sleepTimer = sleepTimer,
    scope = testScope.backgroundScope,
  )

  @Test
  fun `re-enables when it was left on last session`() = testScope.runTest {
    sleepTimerPreferenceStore.updateData { prefs(enabledLastSession = true) }

    sut.onAppStart(mockk())
    playStateManager.playState = PlayStateManager.PlayState.Playing
    advanceUntilIdle()
    yield()

    coVerify { sleepTimer.enable(SleepTimerMode.TimedWithDefault) }
  }

  @Test
  fun `does nothing when it was switched off last session`() = testScope.runTest {
    sleepTimerPreferenceStore.updateData { prefs(enabledLastSession = false) }

    sut.onAppStart(mockk())
    playStateManager.playState = PlayStateManager.PlayState.Playing
    advanceUntilIdle()
    yield()

    coVerify(exactly = 0) { sleepTimer.enable(any()) }
  }

  @Test
  fun `does nothing when the sleep timer is already running`() = testScope.runTest {
    sleepTimerPreferenceStore.updateData { prefs(enabledLastSession = true) }
    sleepTimer.enable(SleepTimerMode.TimedWithDefault)

    sut.onAppStart(mockk())
    playStateManager.playState = PlayStateManager.PlayState.Playing
    advanceUntilIdle()
    yield()

    coVerify(exactly = 1) { sleepTimer.enable(any()) }
  }
}
