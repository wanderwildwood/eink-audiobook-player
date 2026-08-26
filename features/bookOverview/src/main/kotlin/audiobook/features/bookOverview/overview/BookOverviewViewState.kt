package audiobook.features.bookOverview.overview

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import audiobook.core.data.BookId
import audiobook.core.data.LibraryOrganization
import audiobook.features.bookOverview.search.BookSearchViewState

@Immutable
data class BookOverviewViewState(
  val books: Map<BookOverviewCategory, Map<BookId, State<BookOverviewItemViewState>>>,
  val playButtonState: PlayButtonState?,
  val showAddBookHint: Boolean,
  val showSearchIcon: Boolean,
  val isLoading: Boolean,
  val searchActive: Boolean,
  val searchViewState: BookSearchViewState,
  val showStoragePermissionBugCard: Boolean,
  val showFolderPickerIcon: Boolean,
  val dialog: Dialog?,
  val folders: List<AuthorFolderViewState> = emptyList(),
  val nowPlaying: State<BookOverviewItemViewState>? = null,
  val libraryOrganization: LibraryOrganization = LibraryOrganization.AUTHOR_FOLDERS,
  val sections: List<LibrarySection> = emptyList(),
  // True while the media scanner is running, so pull-to-refresh can show its spinner.
  val isRefreshing: Boolean = false,
) {

  companion object {
    val Loading = BookOverviewViewState(
      books = mapOf(),
      playButtonState = null,
      showAddBookHint = false,
      showSearchIcon = false,
      isLoading = true,
      searchActive = false,
      searchViewState = BookSearchViewState.EmptySearch(
        suggestedAuthors = emptyList(),
        recentQueries = emptyList(),
        query = "",
      ),
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = true,
      dialog = null,
    )
  }

  enum class PlayButtonState {
    Playing,
    Paused,
  }

  enum class Dialog {
    FolderPickerMovedToSettings,
  }
}
