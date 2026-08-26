package audiobook.core.playback.session

import audiobook.core.data.Book
import audiobook.core.data.BookId
import audiobook.core.data.Chapter
import audiobook.core.data.ChapterId
import audiobook.core.data.ChapterMark
import audiobook.core.data.durationMs
import audiobook.core.data.markForPosition

internal data class PlaybackItem(
  val index: Int,
  val bookId: BookId,
  val chapter: Chapter,
  val markIndex: Int,
  val mark: ChapterMark,
) {
  val mediaId: MediaId.ChapterMark
    get() = MediaId.ChapterMark(
      bookId = bookId,
      chapterId = chapter.id,
      markIndex = markIndex,
      startMs = mark.startMs,
      endMs = mark.endMs,
    )
}

internal fun Book.playbackItems(): List<PlaybackItem> {
  var index = 0
  return chapters.flatMap { chapter ->
    chapter.chapterMarks.mapIndexed { markIndex, mark ->
      PlaybackItem(
        index = index++,
        bookId = id,
        chapter = chapter,
        markIndex = markIndex,
        mark = mark,
      )
    }
  }
}

internal fun Book.playbackItemForPosition(
  chapterId: ChapterId,
  positionInChapterMs: Long,
): PlaybackItem? {
  val chapter = chapters.firstOrNull { it.id == chapterId } ?: return null
  val mark = chapter.markForPosition(positionInChapterMs)
  return playbackItems().firstOrNull {
    it.chapter.id == chapterId && it.mark == mark
  }
}

internal val MediaId.bookId: BookId?
  get() = when (this) {
    is MediaId.Book -> id
    is MediaId.Chapter -> bookId
    is MediaId.ChapterMark -> bookId
    MediaId.Recent,
    MediaId.Root,
    -> null
  }

internal val MediaId.realChapterId: ChapterId?
  get() = when (this) {
    is MediaId.Chapter -> chapterId
    is MediaId.ChapterMark -> chapterId
    is MediaId.Book,
    MediaId.Recent,
    MediaId.Root,
    -> null
  }

internal fun MediaId.positionInChapter(positionInCurrentMediaItemMs: Long): Long? {
  return when (this) {
    is MediaId.Chapter -> positionInCurrentMediaItemMs
    is MediaId.ChapterMark -> startMs + positionInCurrentMediaItemMs
    is MediaId.Book,
    MediaId.Recent,
    MediaId.Root,
    -> null
  }
}

internal fun PlaybackItem.positionInMediaItem(positionInChapterMs: Long): Long {
  return (positionInChapterMs - mark.startMs).coerceIn(0L, mark.durationMs)
}
