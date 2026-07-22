package voice.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import voice.core.strings.R as StringsR

@Composable
fun PlayButton(
  playing: Boolean,
  fabSize: Dp,
  iconSize: Dp,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
  sharedElementModifier: Modifier = Modifier,
) {
  val cornerSize = if (playing) 16.dp else fabSize / 2
  FloatingActionButton(
    modifier = modifier
      .size(fabSize)
      .then(sharedElementModifier),
    onClick = onPlayClick,
    shape = RoundedCornerShape(cornerSize),
  ) {
    Icon(
      modifier = Modifier.size(iconSize),
      painter = painterResource(id = if (playing) R.drawable.ic_pause_static else R.drawable.ic_play_static),
      contentDescription = stringResource(
        id = if (playing) {
          StringsR.string.playback_action_pause
        } else {
          StringsR.string.playback_action_play
        },
      ),
    )
  }
}
