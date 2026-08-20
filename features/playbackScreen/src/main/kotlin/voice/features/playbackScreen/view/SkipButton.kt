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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons

/**
 * One transport control: a glyph with the distance it travels printed underneath.
 *
 * Both pairs are drawn from their "backwards" glyph mirrored horizontally, so the two
 * directions cannot drift apart visually.
 */
@Composable
private fun TransportButton(
  icon: ImageVector,
  mirrored: Boolean,
  label: String,
  contentDescription: String,
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false),
        onClick = onClick,
      )
      // One target, one announcement - otherwise a screen reader reads the glyph and the
      // label as two separate controls.
      .clearAndSetSemantics { },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      modifier = Modifier
        .size(36.dp)
        .scale(scaleX = if (mirrored) -1f else 1f, scaleY = 1f),
      imageVector = icon,
      contentDescription = contentDescription,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
    )
  }
}

/** Jumps [seconds] within the chapter. Double triangles, the traditional scan glyph. */
@Composable
internal fun SkipButton(
  forward: Boolean,
  seconds: Int,
  onClick: () -> Unit,
) {
  TransportButton(
    icon = VoiceIcons.FastRewind,
    mirrored = forward,
    label = "${seconds}s",
    contentDescription = stringResource(
      id = if (forward) R.string.playback_action_fast_forward else R.string.playback_action_rewind,
    ),
    onClick = onClick,
  )
}

/** Jumps a whole chapter. Triangle against a bar, which is what that glyph has always meant. */
@Composable
internal fun ChapterSkipButton(
  forward: Boolean,
  onClick: () -> Unit,
) {
  TransportButton(
    icon = VoiceIcons.SkipPrevious,
    mirrored = forward,
    label = stringResource(id = R.string.playback_action_chapter),
    contentDescription = stringResource(
      id = if (forward) R.string.playback_chapter_next else R.string.playback_chapter_previous,
    ),
    onClick = onClick,
  )
}
