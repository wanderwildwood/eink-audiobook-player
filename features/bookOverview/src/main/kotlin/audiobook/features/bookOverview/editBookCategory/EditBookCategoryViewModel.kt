package audiobook.features.bookOverview.editBookCategory

import audiobook.core.data.BookId
import audiobook.core.data.repo.BookRepository
import audiobook.features.bookOverview.bottomSheet.BottomSheetItem
import audiobook.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import audiobook.features.bookOverview.di.BookOverviewScope
import audiobook.features.bookOverview.overview.BookOverviewCategory
import audiobook.features.bookOverview.overview.category
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class EditBookCategoryViewModel(private val repo: BookRepository) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    val book = repo.get(bookId) ?: return emptyList()
    return when (book.category) {
      BookOverviewCategory.CURRENT -> listOf(
        BottomSheetItem.BookCategoryMarkAsNotStarted,
        BottomSheetItem.BookCategoryMarkAsCompleted,
      )
      BookOverviewCategory.NOT_STARTED -> listOf(
        BottomSheetItem.BookCategoryMarkAsCurrent,
        BottomSheetItem.BookCategoryMarkAsCompleted,
      )
      BookOverviewCategory.FINISHED -> listOf(
        BottomSheetItem.BookCategoryMarkAsCurrent,
        BottomSheetItem.BookCategoryMarkAsNotStarted,
      )
    }
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    val book = repo.get(bookId) ?: return

    val (currentChapter, positionInChapter) = when (item) {
      BottomSheetItem.BookCategoryMarkAsCurrent -> {
        book.chapters.first().id to 1L
      }
      BottomSheetItem.BookCategoryMarkAsNotStarted -> {
        book.chapters.first().id to 0L
      }
      BottomSheetItem.BookCategoryMarkAsCompleted -> {
        val lastChapter = book.chapters.last()
        lastChapter.id to lastChapter.duration
      }
      else -> return
    }

    repo.updateBook(book.id) {
      it.copy(
        currentChapter = currentChapter,
        positionInChapter = positionInChapter,
      )
    }
  }
}
