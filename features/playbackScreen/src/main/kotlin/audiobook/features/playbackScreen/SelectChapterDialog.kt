package audiobook.features.playbackScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import audiobook.core.ui.StaticBottomSheet
import audiobook.core.strings.R as StringsR

@Composable
internal fun SelectChapterDialog(
  dialogState: BookPlayDialogViewState.SelectChapterDialog,
  viewModel: BookPlayViewModel,
) {
  StaticBottomSheet(
    onDismissRequest = { viewModel.dismissDialog() },
    content = {
      val selectedIndex = dialogState.items.indexOfFirst { it.active }
      // -1 because we want to show the previous chapter on the screen
      val initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0)
      LazyColumn(
        state = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleItemIndex),
        content = {
          items(dialogState.items) { chapter ->
            val description = stringResource(StringsR.string.playback_chapter_current_content_description)
            // The chapter being played fills its row. Setting only the container leaves the three
            // pieces of text at their default onSurface, which on a monochrome scheme is the same
            // black as the fill - so the row people most need to find was the one they could not
            // read. Every content colour is flipped with the container, or neither is.
            val colors = if (chapter.active) {
              ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                headlineColor = MaterialTheme.colorScheme.onPrimaryContainer,
                leadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                trailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            } else {
              ListItemDefaults.colors(containerColor = Color.Transparent)
            }
            ListItem(
              colors = colors,
              modifier = Modifier
                .padding(3.dp)
                .clip(shape = RoundedCornerShape(12.dp))
                .semantics {
                  selected = chapter.active
                  if (chapter.active) contentDescription = description
                }
                .clickable {
                  viewModel.onChapterClick(number = chapter.number)
                },
              headlineContent = {
                Text(text = chapter.name)
              },
              leadingContent = {
                Text(text = chapter.number.toString())
              },
              trailingContent = {
                Text(text = chapter.time)
              },
            )
          }
        },
      )
    },
  )
}
