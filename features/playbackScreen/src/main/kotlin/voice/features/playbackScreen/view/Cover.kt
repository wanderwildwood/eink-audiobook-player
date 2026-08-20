package voice.features.playbackScreen.view

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.core.data.BookId
import voice.core.ui.sharedCoverElementModifier
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun Cover(
  bookId: BookId,
  onDoubleClick: () -> Unit,
  cover: String?,
) {
  AsyncImage(
    modifier = Modifier
      .fillMaxSize()
      .sharedCoverElementModifier(bookId)
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = {
            onDoubleClick()
          },
        )
      }
      .clip(RoundedCornerShape(20.dp)),
    // Fit, not Crop. These covers are square and small - the widest slot they were given
    // letterboxed them, which threw away the title and author printed across the top and
    // bottom, the only part that tells you which book this is at a glance.
    contentScale = ContentScale.Fit,
    model = cover,
    placeholder = painterResource(id = UiR.drawable.album_art),
    error = painterResource(id = UiR.drawable.album_art),
    contentDescription = stringResource(id = StringsR.string.cover_title),
  )
}
