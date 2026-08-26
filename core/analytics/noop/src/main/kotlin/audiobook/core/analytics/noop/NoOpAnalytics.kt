package audiobook.core.analytics.noop

import audiobook.core.analytics.api.Analytics
import audiobook.core.logging.api.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class NoOpAnalytics : Analytics {

  override fun screenView(screenName: String) {
    Logger.v("screenView($screenName)")
  }

  override fun event(
    name: String,
    params: Map<String, String>,
  ) {
    Logger.v("event(name=$name, params=$params)")
  }
}
