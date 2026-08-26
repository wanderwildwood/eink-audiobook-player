package audiobook.core.logging.debug

import android.app.Application
import audiobook.core.initializer.AppInitializer
import audiobook.core.logging.api.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class DebugLogWriterInitializer : AppInitializer {

  override fun onAppStart(application: Application) {
    Logger.install(DebugLogWriter())
  }
}
