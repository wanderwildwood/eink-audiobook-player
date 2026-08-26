package audiobook.core.remoteconfig.api

import android.app.Application
import audiobook.core.common.DispatcherProvider
import audiobook.core.common.MainScope
import audiobook.core.initializer.AppInitializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.launch

@ContributesIntoSet(AppScope::class)
class LoadRemoteConfigOnAppStart(
  private val remoteConfig: RemoteConfig,
  dispatcherProvider: DispatcherProvider,
) : AppInitializer {

  private val mainScope = MainScope(dispatcherProvider)

  override fun onAppStart(application: Application) {
    mainScope.launch {
      remoteConfig.refresh()
    }
  }
}
