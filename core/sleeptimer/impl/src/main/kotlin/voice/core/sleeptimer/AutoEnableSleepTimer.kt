package voice.core.sleeptimer

import android.app.Application
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.initializer.AppInitializer
import voice.core.playback.playstate.PlayStateManager
import voice.core.playback.playstate.PlayStateManager.PlayState.Playing
import java.time.Clock

@ContributesIntoSet(AppScope::class)
class AutoEnableSleepTimer(
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  private val playStateManager: PlayStateManager,
  private val sleepTimer: SleepTimer,
  private val clock: Clock,
  private val createBookmarkAtCurrentPosition: CreateBookmarkAtCurrentPosition,
  private val scope: CoroutineScope,
) : AppInitializer {

  override fun onAppStart(application: Application) {
    playStateManager.playStateFlow
      .filter { it == Playing }
      .onEach {
        if (sleepTimer.state.value.enabled) return@onEach
        val preference = sleepTimerPreferenceStore.data.first()
        when {
          // Re-arming what the user left on isn't a "you fell asleep here" moment, so it gets no
          // bookmark - otherwise every resume would add one.
          preference.enabledLastSession -> sleepTimer.enable(SleepTimerMode.TimedWithDefault)
          shouldEnableNightlySleepTimer(preference) -> {
            sleepTimer.enable(SleepTimerMode.TimedWithDefault)
            createBookmark()
          }
        }
      }
      .launchIn(scope)
  }

  private suspend fun createBookmark() {
    createBookmarkAtCurrentPosition.create()
  }

  private fun shouldEnableNightlySleepTimer(autoSleepTimer: SleepTimerPreference): Boolean {
    return autoSleepTimer.autoSleepTimerEnabled &&
      isTimeInRange(
        currentTime = clock.instant().atZone(clock.zone).toLocalTime(),
        startTime = autoSleepTimer.autoSleepStartTime,
        endTime = autoSleepTimer.autoSleepEndTime,
      )
  }
}
