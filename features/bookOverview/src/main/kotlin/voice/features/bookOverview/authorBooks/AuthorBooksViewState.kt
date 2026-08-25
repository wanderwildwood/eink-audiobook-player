package voice.features.bookOverview.authorBooks

import androidx.compose.runtime.Immutable
import voice.features.bookOverview.overview.BookOverviewItemViewState
import voice.features.bookOverview.overview.BookOverviewLayoutMode

@Immutable
data class AuthorBooksViewState(
  /** What the shelf is called: the author folder, or the genre. */
  val title: String?,
  val books: List<BookOverviewItemViewState>,
)
