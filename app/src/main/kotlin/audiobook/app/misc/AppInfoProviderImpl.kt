package audiobook.app.misc

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import audiobook.app.BuildConfig
import audiobook.core.common.AppInfoProvider
import dev.zacsweers.metro.Inject
import kotlin.time.Instant

@Inject
class AppInfoProviderImpl(private val application: Application) : AppInfoProvider {
  override val versionName: String = BuildConfig.VERSION_NAME
  override val installTime: Instant by lazy {
    val packageManager = application.packageManager
    val packageInfo = if (Build.VERSION.SDK_INT >= 33) {
      packageManager.getPackageInfo(application.packageName, PackageManager.PackageInfoFlags.of(0L))
    } else {
      packageManager.getPackageInfo(application.packageName, 0)
    }
    Instant.fromEpochMilliseconds(packageInfo.firstInstallTime)
  }
}
