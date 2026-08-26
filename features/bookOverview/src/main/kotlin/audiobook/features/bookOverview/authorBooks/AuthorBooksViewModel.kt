package audiobook.features.bookOverview.authorBooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import audiobook.core.data.BookComparator
import audiobook.core.data.BookId
import audiobook.core.data.GridMode
import audiobook.core.data.repo.BookRepository
import audiobook.core.data.shelfAuthor
import audiobook.core.data.store.GridModeStore
import audiobook.core.ui.GridCount
import audiobook.features.bookOverview.overview.BookOverviewLayoutMode
import audiobook.features.bookOverview.overview.toItemViewState
import audiobook.navigation.Destination
import audiobook.navigation.Navigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class AuthorBooksViewModel(
  private val repo: BookRepository,
  private val navigator: Navigator,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  private val gridCount: GridCount,
  @Assisted
  private val name: String?,
  @Assisted
  private val shelf: Destination.AuthorBooks.Shelf,
) {

  @Composable
  fun state(): AuthorBooksViewState {
    val books = remember { repo.flow() }.collectAsState(initial = emptyList()).value
    val gridMode = remember { gridModeStore.data }.collectAsState(initial = null).value

    return AuthorBooksViewState(
      title = name,
      books = books
        .filter {
          name == when (shelf) {
            Destination.AuthorBooks.Shelf.AUTHOR_FOLDER -> it.content.shelfAuthor
            Destination.AuthorBooks.Shelf.GENRE -> it.content.genre
          }
        }
        .sortedWith(BookComparator.ByName)
        .map { it.toItemViewState() },
    )
  }

  fun onBookClick(id: BookId) {
    navigator.goTo(Destination.Playback(id))
  }

  fun onBackClick() {
    navigator.goBack()
  }

  @AssistedFactory
  interface Factory {
    fun create(
      name: String?,
      shelf: Destination.AuthorBooks.Shelf,
    ): AuthorBooksViewModel
  }
}
