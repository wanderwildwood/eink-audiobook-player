package voice.features.bookOverview.authorBooks

import androidx.datastore.core.DataStore
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import voice.core.data.Book
import voice.core.data.GridMode
import voice.core.data.repo.BookRepository
import voice.core.ui.GridCount
import voice.features.bookOverview.book
import voice.navigation.Destination
import voice.navigation.Navigator
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorBooksViewModelTest {

  private val books = listOf(
    book(name = "Redwall", genre = "Fantasy", folderName = "Brian Jacques"),
    book(name = "Mossflower", genre = "Fantasy", folderName = "Brian Jacques"),
    book(name = "Cosmos", genre = "Science", folderName = "Carl Sagan"),
    book(name = "Loose", genre = "Science", folderName = null),
    book(name = "Untagged", genre = null, folderName = "Carl Sagan"),
  )

  @Test
  fun `a genre shelf holds the books of that genre, by name`() = runTest {
    assertShelf(
      shelf = Destination.AuthorBooks.Shelf.GENRE,
      name = "Science",
      bookNames = listOf("Cosmos", "Loose"),
    )
  }

  @Test
  fun `the unsorted genre shelf holds the books with no genre, not the ones with no folder`() = runTest {
    assertShelf(
      shelf = Destination.AuthorBooks.Shelf.GENRE,
      name = null,
      bookNames = listOf("Untagged"),
    )
  }

  @Test
  fun `an author folder shelf holds the books of that folder`() = runTest {
    assertShelf(
      shelf = Destination.AuthorBooks.Shelf.AUTHOR_FOLDER,
      name = "Carl Sagan",
      bookNames = listOf("Cosmos", "Untagged"),
    )
  }

  @Test
  fun `the unsorted author folder shelf holds the books with no folder, not the ones with no genre`() = runTest {
    assertShelf(
      shelf = Destination.AuthorBooks.Shelf.AUTHOR_FOLDER,
      name = null,
      bookNames = listOf("Loose"),
    )
  }

  private suspend fun TestScope.assertShelf(
    shelf: Destination.AuthorBooks.Shelf,
    name: String?,
    bookNames: List<String>,
  ) {
    val viewModel = AuthorBooksViewModel(
      repo = mockk<BookRepository> {
        every { flow() } returns MutableStateFlow(books)
      },
      navigator = mockk<Navigator>(),
      gridModeStore = MemoryDataStore(GridMode.LIST),
      gridCount = mockk<GridCount> {
        every { useGridAsDefault() } returns false
      },
      name = name,
      shelf = shelf,
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.state()
    }.test {
      // The library reaches the state on the recomposition after the first, which composes with
      // the empty initial value.
      assertEquals(expected = emptyList(), actual = awaitItem().books)
      val viewState = awaitItem()
      assertEquals(expected = name, actual = viewState.title)
      assertEquals(expected = bookNames, actual = viewState.books.map { it.name })
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
