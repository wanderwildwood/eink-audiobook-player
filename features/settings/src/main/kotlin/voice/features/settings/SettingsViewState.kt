package voice.features.settings

import voice.core.data.LibraryOrganization
import voice.core.data.sleeptimer.ShakeSensitivity

data class SettingsViewState(
  val seekTimeInSeconds: Int,
  val autoRewindInSeconds: Int,
  val appVersion: String,
  val dialog: Dialog?,
  val libraryOrganization: LibraryOrganization,
  val sleepTimerDurationMinutes: Int,
  val sleepTimerEndOfChapter: Boolean,
  val shakeSensitivity: ShakeSensitivity,
  val showAnalyticSetting: Boolean,
  val analyticsEnabled: Boolean,
  val showDeveloperMenu: Boolean,
  val kioskMode: Boolean,
) {

  enum class Dialog {
    AutoRewindAmount,
    SeekTime,
    SleepTimerDuration,
    ShakeSensitivity,
    LibraryOrganization,
  }

  companion object {
    fun preview(): SettingsViewState {
      return SettingsViewState(
        seekTimeInSeconds = 42,
        autoRewindInSeconds = 12,
        dialog = null,
        appVersion = "1.2.3",
        libraryOrganization = LibraryOrganization.AUTHOR_FOLDERS,
        sleepTimerDurationMinutes = 10,
        sleepTimerEndOfChapter = false,
        shakeSensitivity = ShakeSensitivity.Default,
        analyticsEnabled = false,
        showAnalyticSetting = true,
        showDeveloperMenu = true,
        kioskMode = false,
      )
    }
  }
}
