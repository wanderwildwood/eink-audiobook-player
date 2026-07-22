package voice.features.settings

import androidx.datastore.core.DataStore
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import voice.core.common.AppInfoProvider
import voice.core.common.DispatcherProvider
import voice.core.data.GridMode
import voice.core.data.LibraryOrganization
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.featureflag.MemoryFeatureFlag
import voice.core.ui.GridCount
import voice.navigation.Destination
import voice.navigation.Navigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class SettingsViewModelTest {

  private val scope = TestScope()
  private val autoRewindAmountStore = MemoryDataStore(10)
  private val seekTimeStore = MemoryDataStore(30)
  private val gridModeStore = MemoryDataStore(GridMode.GRID)
  private val libraryOrganizationStore = MemoryDataStore(LibraryOrganization.AUTHOR_FOLDERS)
  private val sleepTimerPreferenceStore = MemoryDataStore(SleepTimerPreference.Default)
  private val analyticsConsentStore = MemoryDataStore(false)
  private val developerMenuUnlockedStore = MemoryDataStore(false)
  private val navigator = mockk<Navigator> {
    every { goTo(any()) } just Runs
  }
  private val appInfoProvider = mockk<AppInfoProvider> {
    every { versionName } returns "1.2.3"
    every { analyticsIncluded } returns true
    every { installTime } returns Instant.parse("2026-06-01T00:00:00Z")
  }
  private val gridCount = mockk<GridCount> {
    every { useGridAsDefault() } returns true
  }
  private val kioskModeFeatureFlag = MemoryFeatureFlag(false)

  private val viewModel = SettingsViewModel(
    autoRewindAmountStore = autoRewindAmountStore,
    seekTimeStore = seekTimeStore,
    navigator = navigator,
    appInfoProvider = appInfoProvider,
    gridModeStore = gridModeStore,
    libraryOrganizationStore = libraryOrganizationStore,
    sleepTimerPreferenceStore = sleepTimerPreferenceStore,
    analyticsConsentStore = analyticsConsentStore,
    gridCount = gridCount,
    kioskModeFeatureFlag = kioskModeFeatureFlag,
    developerMenuUnlockedStore = developerMenuUnlockedStore,
    dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
  )

  @Test
  fun `developer menu is hidden until app version tapped 13 times`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = false, actual = awaitItem().showDeveloperMenu)

      repeat(13) {
        viewModel.onAppVersionClick()
      }

      assertEquals(expected = true, actual = awaitItem().showDeveloperMenu)
    }
  }

  @Test
  fun `developer menu unlock emits snackbar effect`() = scope.runTest {
    viewModel.viewEffects.test {
      repeat(13) {
        viewModel.onAppVersionClick()
      }

      assertIs<SettingsViewEffect.DeveloperMenuUnlocked>(awaitItem())
    }
  }

  @Test
  fun `openDeveloperMenu navigates to developer settings`() {
    viewModel.openDeveloperMenu()

    verify(exactly = 1) {
      navigator.goTo(Destination.DeveloperSettings)
    }
  }

  @Test
  fun `openFolderPicker navigates to folder picker`() {
    viewModel.openFolderPicker()

    verify(exactly = 1) {
      navigator.goTo(Destination.FolderPicker)
    }
  }

  @Test
  fun `view state exposes kiosk mode`() = scope.runTest {
    kioskModeFeatureFlag.value = true

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      awaitItem().let {
        assertEquals(expected = true, actual = it.kioskMode)
      }
    }
  }
}

private class MemoryDataStore<T>(initial: T) : DataStore<T> {

  private val value = MutableStateFlow(initial)

  override val data: Flow<T> get() = value

  override suspend fun updateData(transform: suspend (t: T) -> T): T {
    return value.updateAndGet { transform(it) }
  }
}
