package audiobook.features.bookOverview.views

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
internal fun SectionHeader(
  @StringRes headerRes: Int,
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier,
    text = stringResource(id = headerRes),
    style = MaterialTheme.typography.headlineSmall,
  )
}
