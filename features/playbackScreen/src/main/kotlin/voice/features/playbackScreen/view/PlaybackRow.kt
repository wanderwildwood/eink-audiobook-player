package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import voice.core.ui.PlayButton
import voice.core.ui.playButtonSharedBoundsModifier

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
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    if (showChapterButtons) {
      ChapterSkipButton(forward = false, onClick = onSkipToPrevious)
    }
    SkipButton(forward = false, seconds = seekTimeSeconds, onClick = onRewindClick)
    PlayButton(
      playing = playing,
      fabSize = 80.dp,
      iconSize = 36.dp,
      onPlayClick = onPlayClick,
      sharedElementModifier = Modifier.playButtonSharedBoundsModifier(),
    )
    SkipButton(forward = true, seconds = seekTimeSeconds, onClick = onFastForwardClick)
    if (showChapterButtons) {
      ChapterSkipButton(forward = true, onClick = onSkipToNext)
    }
  }
}
