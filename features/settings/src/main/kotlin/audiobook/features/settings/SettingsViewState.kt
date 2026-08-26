package audiobook.features.settings

import audiobook.core.data.LibraryOrganization
import audiobook.core.data.sleeptimer.ShakeSensitivity

data class SettingsViewState(
  val seekTimeInSeconds: Int,
  val autoRewindInSeconds: Int,
  val appVersion: String,
  val dialog: Dialog?,
  val libraryOrganization: LibraryOrganization,
  val sleepTimerDurationMinutes: Int,
  val sleepTimerEndOfChapter: Boolean,
  val shakeSensitivity: ShakeSensitivity,
  val showDeveloperMenu: Boolean,
) {

  enum class Dialog {
    About,
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
        showDeveloperMenu = true,
      )
    }
  }
}
