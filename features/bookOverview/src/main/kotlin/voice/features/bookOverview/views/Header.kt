package voice.features.bookOverview.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun BrowseHeader(modifier: Modifier = Modifier) {
  Text(
    modifier = modifier,
    text = stringResource(id = StringsR.string.library_browse_title),
    style = MaterialTheme.typography.headlineSmall,
  )
}
