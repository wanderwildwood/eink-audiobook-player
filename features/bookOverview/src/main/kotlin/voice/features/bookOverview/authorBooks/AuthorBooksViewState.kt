package voice.features.bookOverview.authorBooks

import androidx.compose.runtime.Immutable
import voice.features.bookOverview.overview.BookOverviewItemViewState
import voice.features.bookOverview.overview.BookOverviewLayoutMode

@Immutable
data class AuthorBooksViewState(
  val folderName: String?,
  val books: List<BookOverviewItemViewState>,
  val layoutMode: BookOverviewLayoutMode = BookOverviewLayoutMode.List,
)
