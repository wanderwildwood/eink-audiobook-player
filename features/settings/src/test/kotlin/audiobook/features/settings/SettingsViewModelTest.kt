package audiobook.features.settings

import androidx.datastore.core.DataStore
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import audiobook.core.common.AppInfoProvider
import audiobook.core.common.DispatcherProvider
import audiobook.core.data.GridMode
import audiobook.core.data.LibraryOrganization
import audiobook.core.data.sleeptimer.SleepTimerPreference
import audiobook.core.ui.GridCount
import audiobook.navigation.Destination
import audiobook.navigation.Navigator
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
  private val developerMenuUnlockedStore = MemoryDataStore(false)
  private val navigator = mockk<Navigator> {
    every { goTo(any()) } just Runs
  }
  private val appInfoProvider = mockk<AppInfoProvider> {
    every { versionName } returns "1.2.3"
    every { installTime } returns Instant.parse("2026-06-01T00:00:00Z")
  }
  private val gridCount = mockk<GridCount> {
    every { useGridAsDefault() } returns true
  }

  private val viewModel = SettingsViewModel(
    autoRewindAmountStore = autoRewindAmountStore,
    seekTimeStore = seekTimeStore,
    navigator = navigator,
    appInfoProvider = appInfoProvider,
    gridModeStore = gridModeStore,
    libraryOrganizationStore = libraryOrganizationStore,
    sleepTimerPreferenceStore = sleepTimerPreferenceStore,
    gridCount = gridCount,
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
}

private class MemoryDataStore<T>(initial: T) : DataStore<T> {

  private val value = MutableStateFlow(initial)

  override val data: Flow<T> get() = value

  override suspend fun updateData(transform: suspend (t: T) -> T): T {
    return value.updateAndGet { transform(it) }
  }
}
