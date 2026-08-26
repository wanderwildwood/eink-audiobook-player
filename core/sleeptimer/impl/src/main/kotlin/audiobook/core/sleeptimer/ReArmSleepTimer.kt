package audiobook.core.sleeptimer

import android.app.Application
import androidx.datastore.core.DataStore
import audiobook.core.data.sleeptimer.SleepTimerPreference
import audiobook.core.data.store.SleepTimerPreferenceStore
import audiobook.core.initializer.AppInitializer
import audiobook.core.playback.playstate.PlayStateManager
import audiobook.core.playback.playstate.PlayStateManager.PlayState.Playing
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Brings back a sleep timer that was left switched on. When playback starts and the timer is not
 * already running, one that was on when the last session ended is armed again - at any time of
 * day - so it stays on across reading sessions until it is explicitly switched off.
 */
@ContributesIntoSet(AppScope::class)
class ReArmSleepTimer(
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  private val playStateManager: PlayStateManager,
  private val sleepTimer: SleepTimer,
  private val scope: CoroutineScope,
) : AppInitializer {

  override fun onAppStart(application: Application) {
    playStateManager.playStateFlow
      .filter { it == Playing }
      .onEach {
        if (sleepTimer.state.value.enabled) return@onEach
        if (sleepTimerPreferenceStore.data.first().enabledLastSession) {
          sleepTimer.enable(SleepTimerMode.TimedWithDefault)
        }
      }
      .launchIn(scope)
  }
}
