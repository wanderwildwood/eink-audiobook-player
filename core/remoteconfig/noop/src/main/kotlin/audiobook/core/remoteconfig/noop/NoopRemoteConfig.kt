package audiobook.core.remoteconfig.noop

import audiobook.core.remoteconfig.api.RemoteConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class NoopRemoteConfig : RemoteConfig {

  override fun boolean(
    key: String,
    defaultValue: Boolean,
  ): Boolean = defaultValue

  override fun string(
    key: String,
    defaultValue: String,
  ): String = defaultValue

  override suspend fun refresh() {}
}
