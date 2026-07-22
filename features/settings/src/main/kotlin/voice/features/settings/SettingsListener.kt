package voice.features.settings

import voice.core.data.LibraryOrganization
import java.time.LocalTime

interface SettingsListener {
  fun close()
  fun toggleGrid()
  fun onLibraryOrganizationRowClick()
  fun setLibraryOrganization(organization: LibraryOrganization)
  fun seekAmountChanged(seconds: Int)
  fun onSeekAmountRowClick()
  fun autoRewindAmountChang(seconds: Int)
  fun onAutoRewindRowClick()
  fun dismissDialog()
  fun openFaq()
  fun openSourceCode()
  fun setAutoSleepTimer(checked: Boolean)
  fun setAutoSleepTimerStart(time: LocalTime)
  fun setAutoSleepTimerEnd(time: LocalTime)
  fun onSleepTimerDurationRowClick()
  fun setSleepTimerDurationMinutes(minutes: Int)
  fun setSleepTimerEndOfChapter()
  fun toggleAnalytics()
  fun openFolderPicker()
  fun onAppVersionClick()

  fun openDeveloperMenu()

  companion object {
    fun noop() = object : SettingsListener {
      override fun close() {}
      override fun toggleGrid() {}
      override fun onLibraryOrganizationRowClick() {}
      override fun setLibraryOrganization(organization: LibraryOrganization) {}
      override fun seekAmountChanged(seconds: Int) {}
      override fun onSeekAmountRowClick() {}
      override fun autoRewindAmountChang(seconds: Int) {}
      override fun onAutoRewindRowClick() {}
      override fun dismissDialog() {}
      override fun openFaq() {}
      override fun openSourceCode() {}
      override fun setAutoSleepTimer(checked: Boolean) {}
      override fun setAutoSleepTimerStart(time: LocalTime) {}
      override fun setAutoSleepTimerEnd(time: LocalTime) {}
      override fun onSleepTimerDurationRowClick() {}
      override fun setSleepTimerDurationMinutes(minutes: Int) {}
      override fun setSleepTimerEndOfChapter() {}
      override fun toggleAnalytics() {}
      override fun openFolderPicker() {}
      override fun onAppVersionClick() {}
      override fun openDeveloperMenu() {}
    }
  }
}
