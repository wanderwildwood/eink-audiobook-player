package voice.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import voice.features.bookOverview.views.BookFolderIcon
import voice.features.bookOverview.views.SettingsIcon

@Composable
internal fun ColumnScope.TopBarTrailingIcon(
  searchActive: Boolean,
  showAddBookHint: Boolean,
  showFolderPickerIcon: Boolean,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  if (!searchActive) {
    Row {
      if (showFolderPickerIcon) {
        BookFolderIcon(withHint = showAddBookHint, onClick = onBookFolderClick)
      }
      SettingsIcon(onSettingsClick)
    }
  }
}
