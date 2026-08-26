package audiobook.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import audiobook.core.common.AppInfoProvider
import audiobook.core.common.DispatcherProvider
import audiobook.core.common.MainScope
import audiobook.core.data.GridMode
import audiobook.core.data.LibraryOrganization
import audiobook.core.data.sleeptimer.ShakeSensitivity
import audiobook.core.data.sleeptimer.SleepTimerPreference
import audiobook.core.data.store.AutoRewindAmountStore
import audiobook.core.data.store.DeveloperMenuUnlockedStore
import audiobook.core.data.store.GridModeStore
import audiobook.core.data.store.LibraryOrganizationStore
import audiobook.core.data.store.SeekTimeStore
import audiobook.core.data.store.SleepTimerPreferenceStore
import audiobook.core.featureflag.FeatureFlag
import audiobook.core.ui.GridCount
import audiobook.navigation.Destination
import audiobook.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

@Inject
class SettingsViewModel(
  @AutoRewindAmountStore
  private val autoRewindAmountStore: DataStore<Int>,
  @SeekTimeStore
  private val seekTimeStore: DataStore<Int>,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  @LibraryOrganizationStore
  private val libraryOrganizationStore: DataStore<LibraryOrganization>,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  private val gridCount: GridCount,
  @DeveloperMenuUnlockedStore
  private val developerMenuUnlockedStore: DataStore<Boolean>,
  dispatcherProvider: DispatcherProvider,
) : SettingsListener {

  private val mainScope = MainScope(dispatcherProvider)
  internal val viewEffects: SharedFlow<SettingsViewEffect>
    field = MutableSharedFlow<SettingsViewEffect>(extraBufferCapacity = 1)
  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)
  private var appVersionTapCount = 0

  @Composable
  fun viewState(): SettingsViewState {
    val autoRewindAmount by remember { autoRewindAmountStore.data }.collectAsState(initial = 0)
    val seekTime by remember { seekTimeStore.data }.collectAsState(initial = 0)
    val libraryOrganization by remember { libraryOrganizationStore.data }.collectAsState(
      initial = LibraryOrganization.AUTHOR_FOLDERS,
    )
    val sleepTimerPreference by remember { sleepTimerPreferenceStore.data }.collectAsState(
      initial = SleepTimerPreference.Default,
    )
    val showDeveloperMenu by remember { developerMenuUnlockedStore.data }.collectAsState(initial = false)
    return SettingsViewState(
      seekTimeInSeconds = seekTime,
      autoRewindInSeconds = autoRewindAmount,
      dialog = dialog.value,
      appVersion = appInfoProvider.versionName,
      libraryOrganization = libraryOrganization,
      sleepTimerDurationMinutes = sleepTimerPreference.duration.inWholeMinutes.toInt(),
      sleepTimerEndOfChapter = sleepTimerPreference.endOfChapterEnabled,
      shakeSensitivity = sleepTimerPreference.shakeSensitivity,
      showDeveloperMenu = showDeveloperMenu,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun onLibraryOrganizationRowClick() {
    dialog.value = SettingsViewState.Dialog.LibraryOrganization
  }

  override fun setLibraryOrganization(organization: LibraryOrganization) {
    mainScope.launch {
      libraryOrganizationStore.updateData { organization }
    }
    dialog.value = null
  }

  override fun seekAmountChanged(seconds: Int) {
    mainScope.launch {
      seekTimeStore.updateData { seconds }
    }
  }

  override fun onSeekAmountRowClick() {
    dialog.value = SettingsViewState.Dialog.SeekTime
  }

  override fun autoRewindAmountChang(seconds: Int) {
    mainScope.launch {
      autoRewindAmountStore.updateData { seconds }
    }
  }

  override fun onAutoRewindRowClick() {
    dialog.value = SettingsViewState.Dialog.AutoRewindAmount
  }

  override fun dismissDialog() {
    dialog.value = null
  }

  override fun openSourceCode() {
    navigator.goTo(Destination.Website("https://github.com/wanderwildwood/eink-audiobook-player"))
  }

  override fun onAboutClick() {
    dialog.value = SettingsViewState.Dialog.About
  }

  override fun openFolderPicker() {
    navigator.goTo(Destination.FolderPicker)
  }

  override fun onSleepTimerDurationRowClick() {
    dialog.value = SettingsViewState.Dialog.SleepTimerDuration
  }

  override fun setSleepTimerDurationMinutes(minutes: Int) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(duration = minutes.minutes, endOfChapterEnabled = false)
      }
    }
    dialog.value = null
  }

  override fun setSleepTimerEndOfChapter() {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(endOfChapterEnabled = true)
      }
    }
    dialog.value = null
  }

  override fun onShakeSensitivityRowClick() {
    dialog.value = SettingsViewState.Dialog.ShakeSensitivity
  }

  override fun setShakeSensitivity(sensitivity: ShakeSensitivity) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(shakeSensitivity = sensitivity)
      }
    }
    dialog.value = null
  }

  override fun onAppVersionClick() {
    mainScope.launch {
      if (developerMenuUnlockedStore.data.first()) {
        return@launch
      }
      if (++appVersionTapCount >= 13) {
        developerMenuUnlockedStore.updateData { true }
        viewEffects.emit(SettingsViewEffect.DeveloperMenuUnlocked)
      }
    }
  }

  override fun openDeveloperMenu() {
    navigator.goTo(Destination.DeveloperSettings)
  }
}
