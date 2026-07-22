package voice.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import voice.core.data.LibraryOrganization
import voice.core.ui.NoAnimationAlertDialog
import voice.core.ui.icons.VoiceIcons
import voice.core.strings.R as StringsR

private fun LibraryOrganization.labelRes(): Int = when (this) {
  LibraryOrganization.AUTHOR_FOLDERS -> StringsR.string.library_organization_author_folders
  LibraryOrganization.FLAT_LIST -> StringsR.string.library_organization_flat_list
  LibraryOrganization.BY_STATUS -> StringsR.string.library_organization_by_status
}

@Composable
internal fun LibraryOrganizationRow(
  organization: LibraryOrganization,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .clickable { onClick() }
      .fillMaxWidth(),
    leadingContent = {
      Icon(
        imageVector = VoiceIcons.LibraryBooks,
        contentDescription = stringResource(StringsR.string.settings_library_organization_title),
      )
    },
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_library_organization_title))
    },
    supportingContent = {
      Text(text = stringResource(organization.labelRes()))
    },
  )
}

@Composable
internal fun LibraryOrganizationDialog(
  current: LibraryOrganization,
  onSelect: (LibraryOrganization) -> Unit,
  onDismiss: () -> Unit,
) {
  NoAnimationAlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.settings_library_organization_title))
    },
    text = {
      Column {
        LibraryOrganization.entries.forEach { organization ->
          ListItem(
            modifier = Modifier.clickable { onSelect(organization) },
            leadingContent = {
              RadioButton(selected = organization == current, onClick = { onSelect(organization) })
            },
            headlineContent = {
              Text(stringResource(organization.labelRes()))
            },
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(StringsR.string.common_dialog_ok))
      }
    },
  )
}
