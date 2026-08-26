package audiobook.features.bookOverview.overview

import androidx.datastore.core.DataStore
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import audiobook.core.common.AppInfoProvider
import audiobook.core.common.DispatcherProvider
import audiobook.core.data.Book
import audiobook.core.data.BookId
import audiobook.core.data.GridMode
import audiobook.core.data.LibraryOrganization
import audiobook.core.data.repo.BookContentRepo
import audiobook.core.data.repo.BookRepository
import audiobook.core.data.repo.internals.dao.RecentBookSearchDao
import audiobook.core.featureflag.MemoryFeatureFlag
import audiobook.core.playback.LivePlaybackState
import audiobook.core.playback.PlayerController
import audiobook.core.playback.overlay
import audiobook.core.playback.playstate.PlayStateManager
import audiobook.core.scanner.DeviceHasStoragePermissionBug
import audiobook.core.scanner.MediaScanTrigger
import audiobook.core.search.BookSearch
import audiobook.core.ui.GridCount
import audiobook.features.bookOverview.book
import audiobook.navigation.Destination
import audiobook.navigation.Navigator
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BookOverviewViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)

  @Test
  fun `state updates the current book item from live playback`() = runTest {
    val currentBook = book(name = "Current", time = 1_000)
    val otherBook = book(name = "Other", time = 2_000)
    val livePlaybackFlow = MutableStateFlow<LivePlaybackState?>(null)
    val viewModel = BookOverviewViewModel(
      repo = mockk<BookRepository> {
        every { flow() } returns MutableStateFlow(listOf(currentBook, otherBook))
      },
      mediaScanner = mockk<MediaScanTrigger> {
        every { scannerActive } returns MutableStateFlow(false)
        every { scan(any()) } just Runs
      },
      playStateManager = PlayStateManager(),
      playerController = mockk<PlayerController> {
        every { livePlaybackStateFlow(currentBook.id) } returns livePlaybackFlow
      },
      currentBookStoreDataStore = MemoryDataStore(currentBook.id),
      folderPickerMovedDialogShownStore = MemoryDataStore(false),
      gridModeStore = MemoryDataStore(GridMode.LIST),
      libraryOrganizationStore = MemoryDataStore(LibraryOrganization.AUTHOR_FOLDERS),
      gridCount = mockk<GridCount> {
        every { useGridAsDefault() } returns false
      },
      navigator = mockk<Navigator>(),
      appInfoProvider = appInfoProvider(),
      recentBookSearchDao = mockk<RecentBookSearchDao> {
        every { recentBookSearches() } returns MutableStateFlow(emptyList())
      },
      search = mockk<BookSearch> {
        coEvery { search(any()) } returns emptyList()
      },
      contentRepo = mockk<BookContentRepo>(),
      deviceHasStoragePermissionBug = mockk<DeviceHasStoragePermissionBug> {
        every { hasBug } returns MutableStateFlow(false)
      },
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(false),
      experimentalPlaybackPersistenceFeatureFlag = MemoryFeatureFlag(true),
      dispatcherProvider = dispatcherProvider,
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      val initial = awaitItem()
      val initialCurrentItem = initial.currentBook(currentBook.id)
      val initialOtherItem = initial.currentBook(otherBook.id)
      val initialKeys = initial.books.getValue(BookOverviewCategory.CURRENT).keys.toList()

      assertEquals(expected = currentBook.toItemViewState(), actual = initialCurrentItem)
      assertEquals(expected = otherBook.toItemViewState(), actual = initialOtherItem)

      val livePlaybackState = LivePlaybackState(
        bookId = currentBook.id,
        chapterId = currentBook.chapters.first().id,
        positionMs = 6_000,
        isPlaying = true,
        playbackSpeed = 1F,
      )
      livePlaybackFlow.value = livePlaybackState
      yield()

      assertEquals(expected = initialKeys, actual = initial.books.getValue(BookOverviewCategory.CURRENT).keys.toList())
      assertEquals(expected = currentBook.overlay(livePlaybackState).toItemViewState(), actual = initial.currentBook(currentBook.id))
      assertEquals(expected = initialOtherItem, actual = initial.currentBook(otherBook.id))
      expectNoEvents()
    }
  }

  @Test
  fun `folder picker icon is hidden when folder picker in settings flag is true`() = runTest {
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(true),
      folderPickerMovedDialogShownStore = MemoryDataStore(false),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(expected = false, actual = awaitItem().showFolderPickerIcon)
    }
  }

  @Test
  fun `folder picker icon is shown once when flag is false`() = runTest {
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(false),
      folderPickerMovedDialogShownStore = MemoryDataStore(false),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(expected = true, actual = awaitItem().showFolderPickerIcon)
    }
  }

  @Test
  fun `folder picker icon is hidden when moved dialog was shown`() = runTest {
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(false),
      folderPickerMovedDialogShownStore = MemoryDataStore(true),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(expected = false, actual = awaitItem().showFolderPickerIcon)
    }
  }

  @Test
  fun `folder picker icon is hidden for installs on migration cutoff date`() = runTest {
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(false),
      folderPickerMovedDialogShownStore = MemoryDataStore(false),
      appInfoProvider = appInfoProvider(installTime = Instant.parse("2026-06-17T00:00:00Z")),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(expected = false, actual = awaitItem().showFolderPickerIcon)
    }
  }

  @Test
  fun `folder picker click shows moved dialog instead of navigating`() = runTest {
    val navigator = mockk<Navigator>(relaxed = true)
    val viewModel = viewModel(
      navigator = navigator,
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(false),
      folderPickerMovedDialogShownStore = MemoryDataStore(false),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(expected = null, actual = awaitItem().dialog)

      viewModel.onBookFolderClick()

      assertEquals(expected = BookOverviewViewState.Dialog.FolderPickerMovedToSettings, actual = awaitItem().dialog)
      verify(exactly = 0) {
        navigator.goTo(Destination.FolderPicker)
      }
    }
  }

  @Test
  fun `dismissing moved dialog marks it shown and hides folder picker icon`() = runTest {
    val folderPickerMovedDialogShownStore = MemoryDataStore(false)
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(false),
      folderPickerMovedDialogShownStore = folderPickerMovedDialogShownStore,
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(expected = true, actual = awaitItem().showFolderPickerIcon)

      viewModel.onBookFolderClick()
      assertEquals(expected = BookOverviewViewState.Dialog.FolderPickerMovedToSettings, actual = awaitItem().dialog)

      viewModel.onFolderPickerMovedDialogDismiss()

      val dismissed = awaitItem()
      assertEquals(expected = null, actual = dismissed.dialog)
      if (dismissed.showFolderPickerIcon) {
        assertEquals(expected = false, actual = awaitItem().showFolderPickerIcon)
      } else {
        assertEquals(expected = false, actual = dismissed.showFolderPickerIcon)
      }
    }
  }

  @Test
  fun `genre view shelves the books by genre, ungenred last`() = runTest {
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(true),
      folderPickerMovedDialogShownStore = MemoryDataStore(true),
      libraryOrganization = LibraryOrganization.GENRE,
      books = listOf(
        book(name = "Redwall", genre = "Fantasy"),
        book(name = "Mossflower", genre = "Fantasy"),
        book(name = "Cosmos", genre = "Science"),
        book(name = "Untagged", genre = null),
      ),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(
        expected = listOf(
          LibrarySection.Folders(
            listOf(
              AuthorFolderViewState(folderName = "Fantasy", bookCount = 2),
              AuthorFolderViewState(folderName = "Science", bookCount = 1),
              AuthorFolderViewState(folderName = null, bookCount = 1),
            ),
          ),
        ),
        actual = awaitOrganization(LibraryOrganization.GENRE).sections,
      )
    }
  }

  @Test
  fun `genre view shelves the books by author folder when the setting says so`() = runTest {
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(true),
      folderPickerMovedDialogShownStore = MemoryDataStore(true),
      libraryOrganization = LibraryOrganization.AUTHOR_FOLDERS,
      books = listOf(
        book(name = "Redwall", genre = "Fantasy", folderName = "Brian Jacques"),
        book(name = "Cosmos", genre = "Science", folderName = "Carl Sagan"),
        // Only Author-Book Mode sets a folder name. Without the fallback to the author tag this
        // book, and in the other two folder modes the entire library, lands on one unnamed shelf.
        book(name = "Aurora", genre = "Science Fiction", folderName = null, author = "Kim Stanley Robinson"),
      ),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      assertEquals(expected = BookOverviewViewState.Loading, actual = awaitItem())
      assertEquals(
        expected = listOf(
          LibrarySection.Folders(
            listOf(
              AuthorFolderViewState(folderName = "Brian Jacques", bookCount = 1),
              AuthorFolderViewState(folderName = "Carl Sagan", bookCount = 1),
              AuthorFolderViewState(folderName = "Kim Stanley Robinson", bookCount = 1),
            ),
          ),
        ),
        actual = awaitItem().sections,
      )
    }
  }

  @Test
  fun `a shelf opens as a genre in the genre view`() = runTest {
    val navigator = mockk<Navigator>(relaxed = true)
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(true),
      folderPickerMovedDialogShownStore = MemoryDataStore(true),
      navigator = navigator,
      libraryOrganization = LibraryOrganization.GENRE,
    )

    viewModel.onFolderClick("Fantasy")
    runCurrent()

    verify(exactly = 1) {
      navigator.goTo(Destination.AuthorBooks(name = "Fantasy", shelf = Destination.AuthorBooks.Shelf.GENRE))
    }
  }

  @Test
  fun `the unsorted shelf opens as a genre in the genre view`() = runTest {
    val navigator = mockk<Navigator>(relaxed = true)
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(true),
      folderPickerMovedDialogShownStore = MemoryDataStore(true),
      navigator = navigator,
      libraryOrganization = LibraryOrganization.GENRE,
    )

    viewModel.onFolderClick(null)
    runCurrent()

    verify(exactly = 1) {
      navigator.goTo(Destination.AuthorBooks(name = null, shelf = Destination.AuthorBooks.Shelf.GENRE))
    }
  }

  @Test
  fun `a shelf opens as an author folder otherwise`() = runTest {
    val navigator = mockk<Navigator>(relaxed = true)
    val viewModel = viewModel(
      folderPickerInSettingsFeatureFlag = MemoryFeatureFlag(true),
      folderPickerMovedDialogShownStore = MemoryDataStore(true),
      navigator = navigator,
      libraryOrganization = LibraryOrganization.AUTHOR_FOLDERS,
    )

    viewModel.onFolderClick("Brian Jacques")
    runCurrent()

    verify(exactly = 1) {
      navigator.goTo(
        Destination.AuthorBooks(name = "Brian Jacques", shelf = Destination.AuthorBooks.Shelf.AUTHOR_FOLDER),
      )
    }
  }

  /**
   * The stored organization only reaches the state on a later recomposition - the first one runs
   * with the data store's initial value.
   */
  private suspend fun ReceiveTurbine<BookOverviewViewState>.awaitOrganization(organization: LibraryOrganization): BookOverviewViewState {
    while (true) {
      val item = awaitItem()
      if (item.libraryOrganization == organization) return item
    }
  }

  private fun BookOverviewViewState.currentBook(bookId: BookId): BookOverviewItemViewState {
    return books.getValue(BookOverviewCategory.CURRENT).getValue(bookId).value
  }

  private fun viewModel(
    folderPickerInSettingsFeatureFlag: MemoryFeatureFlag<Boolean>,
    folderPickerMovedDialogShownStore: DataStore<Boolean>,
    navigator: Navigator = mockk(),
    appInfoProvider: AppInfoProvider = appInfoProvider(),
    books: List<Book> = emptyList(),
    libraryOrganization: LibraryOrganization = LibraryOrganization.AUTHOR_FOLDERS,
  ): BookOverviewViewModel {
    return BookOverviewViewModel(
      repo = mockk<BookRepository> {
        every { flow() } returns MutableStateFlow(books)
      },
      mediaScanner = mockk<MediaScanTrigger> {
        every { scannerActive } returns MutableStateFlow(false)
        every { scan(any()) } just Runs
      },
      playStateManager = PlayStateManager(),
      playerController = mockk(),
      currentBookStoreDataStore = MemoryDataStore(null),
      folderPickerMovedDialogShownStore = folderPickerMovedDialogShownStore,
      gridModeStore = MemoryDataStore(GridMode.LIST),
      libraryOrganizationStore = MemoryDataStore(libraryOrganization),
      gridCount = mockk<GridCount> {
        every { useGridAsDefault() } returns false
      },
      navigator = navigator,
      appInfoProvider = appInfoProvider,
      recentBookSearchDao = mockk<RecentBookSearchDao> {
        every { recentBookSearches() } returns MutableStateFlow(emptyList())
      },
      search = mockk<BookSearch> {
        coEvery { search(any()) } returns emptyList()
      },
      contentRepo = mockk<BookContentRepo>(),
      deviceHasStoragePermissionBug = mockk<DeviceHasStoragePermissionBug> {
        every { hasBug } returns MutableStateFlow(false)
      },
      folderPickerInSettingsFeatureFlag = folderPickerInSettingsFeatureFlag,
      experimentalPlaybackPersistenceFeatureFlag = MemoryFeatureFlag(false),
      dispatcherProvider = dispatcherProvider,
    )
  }

  private fun appInfoProvider(installTime: Instant = Instant.parse("2026-06-16T00:00:00Z")): AppInfoProvider {
    return mockk {
      every { this@mockk.installTime } returns installTime
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
