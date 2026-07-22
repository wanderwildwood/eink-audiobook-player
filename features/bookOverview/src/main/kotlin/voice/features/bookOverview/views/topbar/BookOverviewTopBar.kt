package voice.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import voice.core.data.BookId
import voice.core.ui.VoiceTheme
import voice.features.bookOverview.overview.BookOverviewLayoutMode
import voice.features.bookOverview.overview.BookOverviewViewState
import voice.features.bookOverview.search.BookSearchViewState
import kotlin.time.Duration.Companion.seconds
import voice.core.strings.R as StringsR

@Composable
internal fun BookOverviewTopBar(
  viewState: BookOverviewViewState,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onActiveChange: (Boolean) -> Unit,
  onQueryChange: (String) -> Unit,
  onSearchBookClick: (BookId) -> Unit,
) {
  Column {
    val horizontalPadding = if (viewState.searchActive) 0.dp else 16.dp
    BookOverviewSearchBar(
      horizontalPadding = horizontalPadding,
      onQueryChange = onQueryChange,
      onActiveChange = onActiveChange,
      onBookFolderClick = onBookFolderClick,
      onSettingsClick = onSettingsClick,
      onSearchBookClick = onSearchBookClick,
      searchActive = viewState.searchActive,
      showAddBookHint = viewState.showAddBookHint,
      showFolderPickerIcon = viewState.showFolderPickerIcon,
      searchViewState = viewState.searchViewState,
    )
    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(viewState.isLoading) {
      if (viewState.isLoading) {
        delay(3.seconds)
      }
      showLoading = viewState.isLoading
    }
    if (showLoading) {
      Text(
        text = stringResource(id = StringsR.string.library_scanning_label),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, start = 16.dp),
      )
    }
  }
}

@Composable
@Preview
private fun BookOverviewTopBarPreview() {
  VoiceTheme {
    BookOverviewTopBar(
      viewState = BookOverviewViewState(
        books = emptyMap(),
        layoutMode = BookOverviewLayoutMode.List,
        playButtonState = BookOverviewViewState.PlayButtonState.Paused,
        showAddBookHint = true,
        showSearchIcon = true,
        isLoading = true,
        searchActive = true,
        searchViewState = BookSearchViewState.EmptySearch(
          suggestedAuthors = listOf(),
          recentQueries = listOf(),
          query = "",
        ),
        showStoragePermissionBugCard = false,
        showFolderPickerIcon = true,
        dialog = null,
      ),
      onBookFolderClick = {},
      onSettingsClick = {},
      onActiveChange = {},
      onQueryChange = {},
      onSearchBookClick = {},
    )
  }
}
