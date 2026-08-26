package audiobook.core.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import audiobook.core.data.ThemeMode
import audiobook.core.data.store.ThemeModeStore
import audiobook.core.initializer.AppInitializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ContributesIntoSet(AppScope::class)
class UIAppStartInitializer(
  @ThemeModeStore
  private val themeModeStore: DataStore<ThemeMode>,
  private val scope: CoroutineScope,
) : AppInitializer {

  override fun onAppStart(application: Application) {
    themeModeStore.data
      .distinctUntilChanged()
      .onEach { themeMode ->
        val nightMode = when (themeMode) {
          ThemeMode.FollowSystem -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
          ThemeMode.Light -> AppCompatDelegate.MODE_NIGHT_NO
          ThemeMode.Dark -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
      }
      .launchIn(scope)
  }
}
