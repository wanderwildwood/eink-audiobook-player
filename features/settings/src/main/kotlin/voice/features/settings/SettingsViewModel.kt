package voice.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.core.common.AppInfoProvider
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.GridMode
import voice.core.data.LibraryOrganization
import voice.core.data.sleeptimer.ShakeSensitivity
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.AutoRewindAmountStore
import voice.core.data.store.DeveloperMenuUnlockedStore
import voice.core.data.store.GridModeStore
import voice.core.data.store.LibraryOrganizationStore
import voice.core.data.store.SeekTimeStore
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.featureflag.FeatureFlag
import voice.core.ui.GridCount
import voice.navigation.Destination
import voice.navigation.Navigator
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
