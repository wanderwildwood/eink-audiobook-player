package voice.features.bookOverview.editGenre

import voice.core.data.BookId

internal data class EditBookGenreState(
  val genre: String,
  val bookId: BookId,
  /** Genres already used elsewhere in the library, offered so spellings stay consistent. */
  val suggestions: List<String>,
) {

  /** Blank is allowed - it clears the genre, which is how a book gets taken back out of a shelf. */
  val confirmButtonEnabled: Boolean = true
}
