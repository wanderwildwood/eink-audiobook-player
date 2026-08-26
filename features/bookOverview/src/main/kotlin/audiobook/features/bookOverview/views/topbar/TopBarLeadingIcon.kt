package audiobook.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audiobook.core.strings.R
import audiobook.core.ui.icons.Icons

@Composable
internal fun ColumnScope.TopBarLeadingIcon(
  searchActive: Boolean,
  onActiveChange: (Boolean) -> Unit,
) {
  if (searchActive) {
    IconButton(onClick = { onActiveChange(false) }) {
      Icon(
        imageVector = Icons.ArrowBack,
        contentDescription = stringResource(id = R.string.common_action_close),
      )
    }
  } else {
    // "Library" as the page heading, with search sitting immediately after it.
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        modifier = Modifier.padding(start = 4.dp),
        text = stringResource(id = R.string.library_browse_title),
        style = MaterialTheme.typography.headlineSmall,
      )
      IconButton(onClick = { onActiveChange(true) }) {
        Icon(
          imageVector = Icons.Search,
          contentDescription = stringResource(id = R.string.library_search_hint),
        )
      }
    }
  }
}
