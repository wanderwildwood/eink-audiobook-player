package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import voice.core.data.BookId

@Composable
internal fun CoverRow(
  bookId: BookId,
  cover: String?,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier, contentAlignment = Alignment.Center) {
    // Square slot, sized by whichever of the two axes runs out first, so the art keeps its
    // shape whatever the screen is doing.
    Box(
      modifier = Modifier
        .wrapContentSize()
        .aspectRatio(1f),
    ) {
      Cover(bookId = bookId, onDoubleClick = onPlayClick, cover = cover)
    }
  }
}
