package voice.features.bookOverview.authorBooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import voice.core.data.BookComparator
import voice.core.data.BookId
import voice.core.data.GridMode
import voice.core.data.repo.BookRepository
import voice.core.data.store.GridModeStore
import voice.core.ui.GridCount
import voice.features.bookOverview.overview.BookOverviewLayoutMode
import voice.features.bookOverview.overview.toItemViewState
import voice.navigation.Destination
import voice.navigation.Navigator

@AssistedInject
class AuthorBooksViewModel(
  private val repo: BookRepository,
  private val navigator: Navigator,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  private val gridCount: GridCount,
  @Assisted
  private val folderName: String?,
) {

  @Composable
  fun state(): AuthorBooksViewState {
    val books = remember { repo.flow() }.collectAsState(initial = emptyList()).value
    val gridMode = remember { gridModeStore.data }.collectAsState(initial = null).value
    val layoutMode = when (gridMode) {
      GridMode.LIST -> BookOverviewLayoutMode.List
      GridMode.GRID -> BookOverviewLayoutMode.Grid
      GridMode.FOLLOW_DEVICE, null -> if (gridCount.useGridAsDefault()) {
        BookOverviewLayoutMode.Grid
      } else {
        BookOverviewLayoutMode.List
      }
    }

    return AuthorBooksViewState(
      folderName = folderName,
      layoutMode = layoutMode,
      books = books
        .filter { it.content.folderName == folderName }
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
    fun create(folderName: String?): AuthorBooksViewModel
  }
}
