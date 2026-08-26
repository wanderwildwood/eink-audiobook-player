package audiobook.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import audiobook.core.ui.icons.Icons
import audiobook.core.strings.R as StringsR

@Composable
internal fun AboutDialog(
  appVersion: String,
  onSourceCodeClick: () -> Unit,
  onAppVersionClick: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.settings_about_title))
    },
    text = {
      Column {
        ListItem(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppVersionClick() },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          leadingContent = {
            Icon(
              imageVector = Icons.Tag,
              contentDescription = null,
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.settings_about_app_version_title))
          },
          supportingContent = {
            Text(appVersion)
          },
        )
        ListItem(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSourceCodeClick() },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          leadingContent = {
            Icon(
              imageVector = Icons.Code,
              contentDescription = null,
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.settings_support_source_code_title))
          },
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(StringsR.string.common_dialog_ok))
      }
    },
  )
}
