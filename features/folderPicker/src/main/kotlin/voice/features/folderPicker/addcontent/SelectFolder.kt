package voice.features.folderPicker.addcontent

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import voice.core.ui.VoiceTheme
import voice.features.folderPicker.folderPicker.FileTypeSelection
import voice.navigation.Origin
import voice.core.strings.R as StringsR

@Composable
internal fun SelectFolder(
  onBack: () -> Unit,
  onAdd: (FileTypeSelection, Uri) -> Unit,
  origin: Origin,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      SelectFolderAppBar(onBack, origin)
    },
    content = { contentPadding ->
      Column(Modifier.padding(contentPadding)) {
        Spacer(Modifier.size(16.dp))

        // The heading used displayMedium, which the e-ink typography does not define, so it fell
        // back to the Material default: 45sp of a face nothing else in the app is set in. Every
        // style here is one the theme actually specifies, flush with the icons below.
        Text(
          modifier = Modifier.padding(horizontal = 20.dp),
          text = stringResource(
            when (origin) {
              Origin.Default -> StringsR.string.folder_add_title_default
              Origin.Onboarding -> StringsR.string.folder_add_title_onboarding
            },
          ),
          style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.size(8.dp))
        Text(
          modifier = Modifier.padding(horizontal = 20.dp),
          text = stringResource(StringsR.string.folder_add_type_subtitle),
          style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.size(24.dp))
        SelectFolderChoices(onAdd)
      }
    },
  )
}

@Composable
@Preview
private fun SelectFolderPreview() {
  VoiceTheme {
    SelectFolder(
      onBack = {},
      onAdd = { _, _ -> },
      origin = Origin.Onboarding,
    )
  }
}
