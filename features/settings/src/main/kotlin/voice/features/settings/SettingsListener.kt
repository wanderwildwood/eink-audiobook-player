package voice.features.settings

import voice.core.data.LibraryOrganization
import voice.core.data.sleeptimer.ShakeSensitivity

interface SettingsListener {
  fun close()
  fun onLibraryOrganizationRowClick()
  fun setLibraryOrganization(organization: LibraryOrganization)
  fun seekAmountChanged(seconds: Int)
  fun onSeekAmountRowClick()
  fun autoRewindAmountChang(seconds: Int)
  fun onAutoRewindRowClick()
  fun dismissDialog()
  fun openSourceCode()
  fun onAboutClick()
  fun onSleepTimerDurationRowClick()
  fun setSleepTimerDurationMinutes(minutes: Int)
  fun setSleepTimerEndOfChapter()
  fun onShakeSensitivityRowClick()
  fun setShakeSensitivity(sensitivity: ShakeSensitivity)
  fun openFolderPicker()
  fun onAppVersionClick()

  fun openDeveloperMenu()

  companion object {
    fun noop() = object : SettingsListener {
      override fun close() {}
      override fun onLibraryOrganizationRowClick() {}
      override fun setLibraryOrganization(organization: LibraryOrganization) {}
      override fun seekAmountChanged(seconds: Int) {}
      override fun onSeekAmountRowClick() {}
      override fun autoRewindAmountChang(seconds: Int) {}
      override fun onAutoRewindRowClick() {}
      override fun dismissDialog() {}
      override fun openSourceCode() {}
      override fun onAboutClick() {}
      override fun onSleepTimerDurationRowClick() {}
      override fun setSleepTimerDurationMinutes(minutes: Int) {}
      override fun setSleepTimerEndOfChapter() {}
      override fun onShakeSensitivityRowClick() {}
      override fun setShakeSensitivity(sensitivity: ShakeSensitivity) {}
      override fun openFolderPicker() {}
      override fun onAppVersionClick() {}
      override fun openDeveloperMenu() {}
    }
  }
}
