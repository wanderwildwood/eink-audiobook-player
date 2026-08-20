package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons

/**
 * Rewind / fast-forward, with the amount they move printed underneath.
 *
 * Without the number these are two bare curved arrows sitting directly below the chapter
 * chevrons, and nothing on screen says whether they step by seconds or by chapters, let alone
 * how far. The label is the whole point of the button, so it is part of the button.
 */
@Composable
internal fun SkipButton(
  forward: Boolean,
  seconds: Int,
  onClick: () -> Unit,
) {
  val label = stringResource(
    id = if (forward) {
      R.string.playback_action_fast_forward
    } else {
      R.string.playback_action_rewind
    },
  )
  Column(
    modifier = Modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false),
        onClick = onClick,
      )
      // One target, one announcement: without this a screen reader reads the icon and the
      // number as two separate things.
      .clearAndSetSemantics { },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      modifier = Modifier
        .size(40.dp)
        .scale(scaleX = if (forward) -1f else 1F, scaleY = 1f),
      imageVector = VoiceIcons.Undo,
      contentDescription = label,
    )
    Text(
      text = "${seconds}s",
      style = MaterialTheme.typography.labelMedium,
    )
  }
}
