package audiobook.features.bookOverview.editTitle

import audiobook.core.data.BookId

internal data class EditBookTitleState(
  val title: String,
  val bookId: BookId,
) {

  val confirmButtonEnabled: Boolean = title.trim().isNotEmpty()
}
