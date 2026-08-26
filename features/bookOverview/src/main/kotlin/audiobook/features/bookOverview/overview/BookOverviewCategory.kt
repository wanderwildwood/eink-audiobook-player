package audiobook.features.bookOverview.overview

import androidx.annotation.StringRes
import audiobook.core.data.Book
import audiobook.core.data.BookComparator
import java.util.concurrent.TimeUnit.SECONDS
import audiobook.core.strings.R as StringsR

enum class BookOverviewCategory(
  @StringRes val nameRes: Int,
  val comparator: Comparator<Book>,
) {
  CURRENT(
    nameRes = StringsR.string.library_category_current_title,
    comparator = BookComparator.ByLastPlayed,
  ),
  NOT_STARTED(
    nameRes = StringsR.string.library_category_not_started_title,
    comparator = BookComparator.ByName,
  ),
  FINISHED(
    nameRes = StringsR.string.library_category_completed_title,
    comparator = BookComparator.ByLastPlayed,
  ),
}

val Book.category: BookOverviewCategory
  get() {
    return if (position == 0L) {
      BookOverviewCategory.NOT_STARTED
    } else {
      if (position >= duration - SECONDS.toMillis(5)) {
        BookOverviewCategory.FINISHED
      } else {
        BookOverviewCategory.CURRENT
      }
    }
  }
