package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import voice.core.data.BookId

@Composable
internal fun CoverRow(
  bookId: BookId,
  cover: String?,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    Cover(bookId = bookId, onDoubleClick = onPlayClick, cover = cover)
  }
}
