package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons

/**
 * The current chapter, and the control that opens the chapter list.
 *
 * Left aligned, so it stacks under the author and title as one block of text rather than
 * floating in the middle of its own line.
 */
@Composable
internal fun ChapterRow(
  chapterName: String,
  nextPreviousVisible: Boolean,
  onCurrentChapterClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onCurrentChapterClick)
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = chapterName,
      style = MaterialTheme.typography.bodyLarge,
    )
    if (nextPreviousVisible) {
      Icon(
        modifier = Modifier
          .padding(start = 4.dp)
          .size(28.dp),
        imageVector = VoiceIcons.ExpandMore,
        contentDescription = stringResource(id = R.string.playback_chapter_next),
      )
    }
  }
}
