package voice.features.folderPicker.selectType

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import voice.core.strings.R as StringsR

/**
 * One book the chosen folder mode would find. A name and a file count is the whole question this
 * screen asks - whether the books it lists are the books you meant - so it is drawn as a plain
 * row, like every other list in the app.
 */
@Composable
internal fun FolderModeBook(
  book: SelectFolderTypeViewState.Book,
  modifier: Modifier = Modifier,
) {
  ListItem(
    modifier = modifier,
    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    headlineContent = {
      Text(text = book.name)
    },
    supportingContent = {
      Text(
        text = pluralStringResource(
          id = StringsR.plurals.folder_type_file_count,
          count = book.fileCount,
          book.fileCount,
        ),
      )
    },
  )
}
