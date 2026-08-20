package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
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
internal fun TransportButton(
  mirrored: Boolean,
  contentDescription: String,
  onClick: () -> Unit,
  icon: ImageVector? = null,
  painter: Painter? = null,
  iconSize: Dp = 36.dp,
  label: String? = null,
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
  ) {
    // Every glyph is centred in a box of the same height, whatever its own size, so all five
    // sit on one line however big they are and whether or not they carry a label underneath.
    Box(
      modifier = Modifier.size(ICON_SLOT),
      contentAlignment = Alignment.Center,
    ) {
      val iconModifier = Modifier
        .size(iconSize)
        .scale(scaleX = if (mirrored) -1f else 1f, scaleY = 1f)
      when {
        painter != null -> Icon(modifier = iconModifier, painter = painter, contentDescription = contentDescription)
        icon != null -> Icon(modifier = iconModifier, imageVector = icon, contentDescription = contentDescription)
      }
    }
    if (label != null) {
      Text(
        text = label,
        // Small on purpose. It is a footnote on the glyph, not a thing to read - the arrows
        // carry the meaning and this only settles how far.
        style = MaterialTheme.typography.labelMedium,
      )
    }
  }
}

private val ICON_SLOT = 56.dp

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
    iconSize = 36.dp,
    contentDescription = stringResource(
      id = if (forward) R.string.playback_action_fast_forward else R.string.playback_action_rewind,
    ),
    onClick = onClick,
  )
}

/**
 * Jumps a whole chapter. Triangle against a bar, which is what that glyph has always meant -
 * well enough known to go unlabelled, unlike a bare arrow that could mean any distance.
 */
@Composable
internal fun ChapterSkipButton(
  forward: Boolean,
  onClick: () -> Unit,
) {
  TransportButton(
    icon = VoiceIcons.SkipPrevious,
    mirrored = forward,
    contentDescription = stringResource(
      id = if (forward) R.string.playback_chapter_next else R.string.playback_chapter_previous,
    ),
    onClick = onClick,
  )
}
