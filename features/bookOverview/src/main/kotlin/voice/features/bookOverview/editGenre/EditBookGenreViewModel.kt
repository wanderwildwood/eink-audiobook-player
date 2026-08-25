package voice.features.bookOverview.editGenre

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.repo.BookRepository
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import voice.features.bookOverview.di.BookOverviewScope

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class EditBookGenreViewModel(private val repo: BookRepository) : BottomSheetItemViewModel {

  private val scope = MainScope()

  private val _state = mutableStateOf<EditBookGenreState?>(null)
  internal val state: State<EditBookGenreState?> get() = _state

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    return listOf(BottomSheetItem.Genre)
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item != BottomSheetItem.Genre) return
    val book = repo.get(bookId) ?: return
    _state.value = EditBookGenreState(
      genre = book.content.genre.orEmpty(),
      bookId = bookId,
      // Whatever the rest of the library already says, so "Sci-Fi" and "Science Fiction" do not
      // end up as two shelves holding half the same books each.
      suggestions = repo.all()
        .mapNotNull { it.content.genre?.trim()?.takeIf(String::isNotEmpty) }
        .distinct()
        .sorted(),
    )
  }

  internal fun onDismissEditGenre() {
    _state.value = null
  }

  internal fun onUpdateEditGenre(genre: String) {
    _state.value = _state.value?.copy(genre = genre)
  }

  internal fun onConfirmEditGenre() {
    val state = _state.value
    if (state != null) {
      scope.launch {
        repo.updateBook(state.bookId) {
          it.copy(genre = state.genre.trim().takeIf(String::isNotEmpty))
        }
      }
    }
    _state.value = null
  }
}
