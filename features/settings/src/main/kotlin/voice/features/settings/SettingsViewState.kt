package voice.features.settings

import voice.core.data.LibraryOrganization
import voice.core.data.sleeptimer.ShakeSensitivity
import java.time.LocalTime

data class SettingsViewState(
  val seekTimeInSeconds: Int,
  val autoRewindInSeconds: Int,
  val appVersion: String,
  val dialog: Dialog?,
  val libraryOrganization: LibraryOrganization,
  val autoSleepTimer: AutoSleepTimerViewState,
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
        autoSleepTimer = AutoSleepTimerViewState.preview(),
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

  data class AutoSleepTimerViewState(
    val enabled: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime,
  ) {
    companion object {
      fun preview(): AutoSleepTimerViewState {
        return AutoSleepTimerViewState(
          enabled = false,
          startTime = LocalTime.of(22, 0),
          endTime = LocalTime.of(6, 0),
        )
      }
    }
  }
}
