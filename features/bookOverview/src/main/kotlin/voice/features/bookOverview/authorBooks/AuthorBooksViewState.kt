package voice.features.bookOverview.authorBooks

import androidx.compose.runtime.Immutable
import voice.features.bookOverview.overview.BookOverviewItemViewState

@Immutable
data class AuthorBooksViewState(
  val folderName: String?,
  val books: List<BookOverviewItemViewState>,
)
