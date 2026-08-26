package audiobook.features.folderPicker.addcontent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * One of the two ways to add books, drawn the way the library draws a folder: a large outlined
 * icon, the label in the app's own type, and the whole width as the target. It used to be a
 * filled pill button - the only one in the app - which read as a control borrowed from somewhere
 * else, and gave a thumb far less to aim at than an e-ink screen wants.
 */
@Composable
internal fun SelectFolderChoice(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 20.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(40.dp),
      imageVector = icon,
      contentDescription = null,
    )
    Text(
      modifier = Modifier.padding(start = 16.dp),
      text = text,
      style = MaterialTheme.typography.titleLarge,
    )
  }
}
