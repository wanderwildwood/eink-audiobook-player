package audiobook.features.bookOverview

import audiobook.core.data.Book
import audiobook.core.data.BookContent
import audiobook.core.data.BookId
import audiobook.core.data.Chapter
import audiobook.core.data.ChapterId
import java.time.Instant
import kotlin.uuid.Uuid

fun book(
  chapters: List<Chapter> = listOf(chapter(), chapter()),
  time: Long = 42,
  currentChapter: ChapterId = chapters.first().id,
  name: String = Uuid.random().toString(),
  author: String? = Uuid.random().toString(),
  genre: String? = null,
  folderName: String? = null,
): Book {
  return Book(
    content = BookContent(
      author = author,
      name = name,
      positionInChapter = time,
      playbackSpeed = 1F,
      addedAt = Instant.EPOCH,
      chapters = chapters.map { it.id },
      cover = null,
      currentChapter = currentChapter,
      isActive = true,
      lastPlayedAt = Instant.EPOCH,
      skipSilence = false,
      id = BookId(Uuid.random().toString()),
      gain = 0F,
      genre = genre,
      narrator = null,
      series = null,
      part = null,
      folderName = folderName,
    ),
    chapters = chapters,
  )
}

fun chapter(
  duration: Long = 10000,
  id: ChapterId = ChapterId(Uuid.random().toString()),
): Chapter {
  return Chapter(
    id = id,
    name = Uuid.random().toString(),
    duration = duration,
    fileLastModified = Instant.EPOCH,
    markData = emptyList(),
    fileSize = 0,
  )
}
