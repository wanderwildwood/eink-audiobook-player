package audiobook.features.bookOverview.di

import audiobook.features.bookOverview.bottomSheet.BottomSheetViewModel
import audiobook.features.bookOverview.deleteBook.DeleteBookViewModel
import audiobook.features.bookOverview.editGenre.EditBookGenreViewModel
import audiobook.features.bookOverview.editTitle.EditBookTitleViewModel
import audiobook.features.bookOverview.overview.BookOverviewViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension

abstract class BookOverviewScope private constructor()

@GraphExtension(scope = BookOverviewScope::class)
interface BookOverviewGraph {
  val bookOverviewViewModel: BookOverviewViewModel
  val editBookTitleViewModel: EditBookTitleViewModel
  val editBookGenreViewModel: EditBookGenreViewModel
  val bottomSheetViewModel: BottomSheetViewModel
  val deleteBookViewModel: DeleteBookViewModel

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  interface Factory {
    fun create(): BookOverviewGraph

    @ContributesTo(AppScope::class)
    interface Provider {
      val bookOverviewGraphProviderFactory: Factory
    }
  }
}
