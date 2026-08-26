package audiobook.core.remoteconfig.noop

import audiobook.core.remoteconfig.api.FmcTokenProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class NoopFmcTokenProvider : FmcTokenProvider {

  override suspend fun token(): String? {
    return null
  }
}
