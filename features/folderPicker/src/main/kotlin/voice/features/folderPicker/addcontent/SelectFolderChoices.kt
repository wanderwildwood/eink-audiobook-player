package voice.features.folderPicker.addcontent

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.logging.api.Logger
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons
import voice.features.folderPicker.folderPicker.FileTypeSelection

@Composable
internal fun SelectFolderChoices(onAdd: (FileTypeSelection, Uri) -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    val openDocumentLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.OpenDocument(),
    ) { uri ->
      if (uri != null) {
        onAdd(FileTypeSelection.File, uri)
      }
    }
    val documentTreeLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
          onAdd(FileTypeSelection.Folder, uri)
        }
      }

    SelectFolderChoice(
      icon = VoiceIcons.AudioFile,
      text = stringResource(id = R.string.folder_add_type_file),
      onClick = {
        try {
          openDocumentLauncher.launch(arrayOf("*/*"))
        } catch (e: ActivityNotFoundException) {
          Logger.w(e, "Could not add file")
        }
      },
    )
    SelectFolderChoice(
      icon = VoiceIcons.Folder,
      text = stringResource(id = R.string.folder_add_type_folder),
      onClick = {
        try {
          documentTreeLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
          Logger.w(e, "Could not add folder")
        }
      },
    )
  }
}
