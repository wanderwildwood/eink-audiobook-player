package audiobook.features.bookOverview.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audiobook.core.data.BookId
import audiobook.core.ui.icons.Icons
import audiobook.core.ui.plus
import audiobook.features.bookOverview.views.ListBookRow
import audiobook.core.strings.R as StringsR

@Composable
internal fun BookSearchContent(
  viewState: BookSearchViewState,
  contentPadding: PaddingValues,
  onQueryChange: (String) -> Unit,
  onBookClick: (BookId) -> Unit,
) {
  when (viewState) {
    is BookSearchViewState.EmptySearch -> {
      LazyColumn(contentPadding = contentPadding) {
        item {
          Spacer(modifier = Modifier.size(16.dp))
        }
        items(viewState.recentQueries) { query ->
          ListItem(
            modifier = Modifier.clickable { onQueryChange(query) },
            headlineContent = { Text(query) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
              Icon(
                imageVector = Icons.History,
                contentDescription = stringResource(id = StringsR.string.cover_search_recent_content_description),
              )
            },
          )
        }
        items(viewState.suggestedAuthors) { author ->
          ListItem(
            modifier = Modifier.clickable { onQueryChange(author) },
            headlineContent = { Text(author) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          )
        }
      }
    }
    is BookSearchViewState.SearchResults -> {
      LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
          .padding(contentPadding)
          .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
          items(viewState.books) { book ->
            ListBookRow(
              book = book,
              onBookClick = onBookClick,
              onBookLongClick = onBookClick,
            )
          }
        },
      )
    }
  }
}
