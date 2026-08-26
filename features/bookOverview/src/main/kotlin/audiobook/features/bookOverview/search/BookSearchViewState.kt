package audiobook.features.bookOverview.search

import androidx.compose.runtime.Immutable
import audiobook.features.bookOverview.overview.BookOverviewItemViewState
import audiobook.features.bookOverview.overview.BookOverviewLayoutMode

@Immutable
sealed interface BookSearchViewState {
  val query: String

  data class SearchResults(
    val books: List<BookOverviewItemViewState>,
    override val query: String,
  ) : BookSearchViewState

  data class EmptySearch(
    val suggestedAuthors: List<String>,
    val recentQueries: List<String>,
    override val query: String,
  ) : BookSearchViewState
}
