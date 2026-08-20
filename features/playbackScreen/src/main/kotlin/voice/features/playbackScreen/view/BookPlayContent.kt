package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import voice.core.data.BookId
import voice.features.playbackScreen.BookPlayViewState
import kotlin.time.Duration

/**
 * Author, book, chapter, then the progress and the controls.
 *
 * There used to be a cover here. On this screen it was a square image stretched across a wide
 * slot and cropped, and the panel has no colour to render it with in the first place - so it
 * cost a third of the page to say less than the words do. What the words say is what you
 * actually want on waking: whose book this is, which book, and where in it you are.
 */
@Composable
internal fun BookPlayContent(
  contentPadding: PaddingValues,
  viewState: BookPlayViewState,
  @Suppress("UNUSED_PARAMETER") bookId: BookId,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSeek: (Duration) -> Unit,
  onSkipToNext: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onCurrentChapterClick: () -> Unit,
  @Suppress("UNUSED_PARAMETER") useLandscapeLayout: Boolean,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(contentPadding)
      .padding(horizontal = 16.dp),
  ) {
    // The text sits in the middle of the page rather than at the top, so it lands where the eye
    // already is when the screen wakes, and so a long title grows into empty space instead of
    // pushing the controls around.
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.Center,
    ) {
      viewState.author?.let { author ->
        Text(
          text = author,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.size(4.dp))
      }
      Text(
        text = viewState.title,
        style = MaterialTheme.typography.headlineMedium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )
    }

    viewState.chapterName?.let { chapterName ->
      ChapterRow(
        chapterName = chapterName,
        nextPreviousVisible = viewState.showPreviousNextButtons,
        onCurrentChapterClick = onCurrentChapterClick,
      )
    }

    SliderRow(
      duration = viewState.duration,
      playedTime = viewState.playedTime,
      onSeek = onSeek,
    )

    Spacer(modifier = Modifier.size(24.dp))

    PlaybackRow(
      playing = viewState.playing,
      seekTimeSeconds = viewState.seekTimeSeconds,
      showChapterButtons = viewState.showPreviousNextButtons,
      onPlayClick = onPlayClick,
      onRewindClick = onRewindClick,
      onFastForwardClick = onFastForwardClick,
      onSkipToPrevious = onSkipToPrevious,
      onSkipToNext = onSkipToNext,
    )

    Spacer(modifier = Modifier.size(32.dp))
  }
}
