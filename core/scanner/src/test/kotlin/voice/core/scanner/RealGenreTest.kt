package voice.core.scanner

import kotlin.test.Test
import kotlin.test.assertEquals

class RealGenreTest {

  @Test
  fun `a genre worth shelving under is kept, trimmed`() {
    assertEquals(expected = "Fantasy", actual = "Fantasy".asRealGenre())
    assertEquals(expected = "Science fiction", actual = "  Science fiction  ".asRealGenre())
  }

  @Test
  fun `nothing at all is not a genre`() {
    assertEquals(expected = null, actual = null.asRealGenre())
    assertEquals(expected = null, actual = "".asRealGenre())
    assertEquals(expected = null, actual = "   ".asRealGenre())
  }

  @Test
  fun `a bare ID3v1 code is not a genre`() {
    assertEquals(expected = null, actual = "(101)".asRealGenre())
    assertEquals(expected = null, actual = "101".asRealGenre())
    assertEquals(expected = null, actual = "12".asRealGenre())
  }

  @Test
  fun `the format is not a genre, whatever case it is written in`() {
    assertEquals(expected = null, actual = "Audiobook".asRealGenre())
    assertEquals(expected = null, actual = "SPOKEN WORD".asRealGenre())
    assertEquals(expected = null, actual = "Unknown".asRealGenre())
    assertEquals(expected = null, actual = "user defined".asRealGenre())
    // The format wins even when a genre is spelled out in front of it.
    assertEquals(expected = null, actual = "Non Fiction Audio Book".asRealGenre())
  }

  @Test
  fun `not being a novel is not a subject, so the slot stays open for one`() {
    assertEquals(expected = null, actual = "Nonfiction".asRealGenre())
    assertEquals(expected = null, actual = "non fiction".asRealGenre())
    assertEquals(expected = null, actual = "Non-Fiction".asRealGenre())
    assertEquals(expected = "Philosophy", actual = "Philosophy".asRealGenre())
  }

  @Test
  fun `a list is not a shelf`() {
    assertEquals(expected = null, actual = "Fiction/Literature/Science Fiction".asRealGenre())
    assertEquals(expected = null, actual = "Electronic, Folk, Modern Classical".asRealGenre())
  }
}
