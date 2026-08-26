package audiobook.features.bookOverview.bottomSheet

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import audiobook.core.ui.icons.Icons
import audiobook.core.strings.R as StringsR

internal data class EditBookBottomSheetState(val items: List<BottomSheetItem>)

enum class BottomSheetItem(
  @StringRes val titleRes: Int,
  val icon: ImageVector,
) {
  Title(StringsR.string.book_edit_name_label, Icons.Title),
  Genre(StringsR.string.book_edit_genre_label, Icons.Tag),
  DeleteBook(StringsR.string.book_delete_bottom_sheet_title, Icons.Delete),
  BookCategoryMarkAsNotStarted(StringsR.string.book_category_action_mark_not_started, Icons.HourglassEmpty),
  BookCategoryMarkAsCurrent(StringsR.string.book_category_action_mark_current, Icons.NotStarted),
  BookCategoryMarkAsCompleted(StringsR.string.book_category_action_mark_completed, Icons.Done),
}
