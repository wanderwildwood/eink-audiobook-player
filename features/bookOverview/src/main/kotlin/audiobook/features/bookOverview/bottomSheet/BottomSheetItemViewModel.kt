package audiobook.features.bookOverview.bottomSheet

import audiobook.core.data.BookId

interface BottomSheetItemViewModel {

  suspend fun items(bookId: BookId): List<BottomSheetItem>
  suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  )
}
