package audiobook.features.playbackScreen.view

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import audiobook.core.strings.R
import audiobook.core.ui.icons.Icons

@Composable
internal fun CloseIcon(onCloseClick: () -> Unit) {
  IconButton(onClick = onCloseClick) {
    Icon(
      imageVector = Icons.Close,
      contentDescription = stringResource(id = R.string.common_action_close),
    )
  }
}
