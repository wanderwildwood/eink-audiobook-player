package audiobook.features.playbackScreen.view

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import audiobook.core.data.BookId
import audiobook.features.playbackScreen.BookPlayViewState
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
    // Takes all the slack, so the block below is pushed down to the bottom of the page. The top
    // icons are in the app bar and are not affected.
    Spacer(modifier = Modifier.weight(1f))

    viewState.author?.let { author ->
      Text(
        text = author,
        style = MaterialTheme.typography.titleLarge,
        // Bold against the italic title beneath it, so the block reads as a name over a work
        // rather than three lines of the same weight.
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.size(4.dp))
    }
    Text(
      text = viewState.title,
      style = MaterialTheme.typography.headlineLarge,
      // Italic, the way a book title is set in print.
      fontStyle = FontStyle.Italic,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
    )

    viewState.chapterName?.let { chapterName ->
      ChapterRow(
        chapterName = chapterName,
        nextPreviousVisible = viewState.showPreviousNextButtons,
        onCurrentChapterClick = onCurrentChapterClick,
      )
    }

    // A gap between the words and the bar. Without it the chapter line sits right on top of
    // the progress, and the two read as one control rather than a heading above a scale.
    Spacer(modifier = Modifier.size(16.dp))

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

    // Fixed, not weighted, so every bit of leftover space falls to the spacer above and the
    // whole block - author down to the transport row - sits at the foot of the page. Just enough
    // to keep the buttons off the bottom edge.
    Spacer(modifier = Modifier.size(24.dp))
  }
}
