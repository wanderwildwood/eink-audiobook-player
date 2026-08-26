package voice.features.folderPicker.addcontent

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons
import voice.navigation.Origin

@Composable
internal fun SelectFolderAppBar(
  onBack: () -> Unit,
  origin: Origin,
) {
  TopAppBar(
    title = { },
    navigationIcon = {
      // On first launch this screen is the entry point, so there is nothing behind it to go
      // back to and the arrow would be a control that does nothing.
      if (origin != Origin.Onboarding) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = VoiceIcons.ArrowBack,
            contentDescription = stringResource(id = R.string.common_action_close),
          )
        }
      }
    },
  )
}
