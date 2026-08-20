package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

/**
 * Every transport control on one row: chapter back, 20s back, play, 20s forward, chapter forward.
 *
 * The chapter buttons used to be chevrons up beside the chapter title, which put two unlabelled
 * pairs of arrows on the screen in different places and left it to the reader to work out that
 * one pair moved by chapter and the other by some number of seconds. Together, in order of how
 * far each one jumps, they explain each other.
 */
@Composable
internal fun PlaybackRow(
  playing: Boolean,
  seekTimeSeconds: Int,
  showChapterButtons: Boolean,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onSkipToNext: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    if (showChapterButtons) {
      ChapterSkipButton(forward = false, onClick = onSkipToPrevious)
    }
    SkipButton(forward = false, seconds = seekTimeSeconds, onClick = onRewindClick)
    PlayPauseButton(playing = playing, onClick = onPlayClick)
    SkipButton(forward = true, seconds = seekTimeSeconds, onClick = onFastForwardClick)
    if (showChapterButtons) {
      ChapterSkipButton(forward = true, onClick = onSkipToNext)
    }
  }
}

/**
 * Play as a glyph rather than a filled button.
 *
 * It used to be a large black circle, which on a monochrome e-ink page is the heaviest thing on
 * the screen by a wide margin - it read as a hole rather than as the primary control, and it
 * did not belong beside four line-drawn arrows. Bigger than its neighbours is enough to say it
 * is the main one.
 */
@Composable
private fun PlayPauseButton(
  playing: Boolean,
  onClick: () -> Unit,
) {
  Icon(
    modifier = Modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false),
        onClick = onClick,
      )
      .size(56.dp),
    painter = painterResource(
      id = if (playing) UiR.drawable.ic_pause_static else UiR.drawable.ic_play_static,
    ),
    contentDescription = stringResource(
      id = if (playing) StringsR.string.playback_action_pause else StringsR.string.playback_action_play,
    ),
  )
}
