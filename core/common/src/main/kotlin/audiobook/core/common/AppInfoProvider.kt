package audiobook.core.common

import kotlin.time.Instant

interface AppInfoProvider {
  val versionName: String

  val installTime: Instant
}
