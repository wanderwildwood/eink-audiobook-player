package audiobook.core.playback.session

import android.app.Application
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import audiobook.core.data.Book
import audiobook.core.data.BookComparator
import audiobook.core.data.BookContent
import audiobook.core.data.BookId
import audiobook.core.data.Chapter
import audiobook.core.data.durationMs
import audiobook.core.data.repo.BookContentRepo
import audiobook.core.data.repo.BookRepository
import audiobook.core.data.repo.ChapterRepo
import audiobook.core.data.store.CurrentBookStore
import audiobook.core.data.toUri
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import audiobook.core.strings.R as StringsR

@Inject
class MediaItemProvider(
  private val bookRepository: BookRepository,
  private val application: Application,
  private val chapterRepo: ChapterRepo,
  private val contentRepo: BookContentRepo,
  @CurrentBookStore
  private val currentBookStoreId: DataStore<BookId?>,
) {

  fun root(): MediaItem = MediaItem(
    title = application.getString(StringsR.string.media_session_library_root),
    browsable = true,
    isPlayable = false,
    mediaId = MediaId.Root,
    mediaType = MediaType.AudioBookRoot,
  )

  fun recent(): MediaItem? = MediaItem(
    title = application.getString(StringsR.string.media_session_library_recent),
    browsable = true,
    isPlayable = false,
    mediaId = MediaId.Recent,
    mediaType = MediaType.AudioBook,
  ).takeIf { runBlocking { currentBookStoreId.data.first() != null } }

  suspend fun item(id: String): MediaItem? {
    val mediaId = id.toMediaIdOrNull() ?: return null
    return when (mediaId) {
      MediaId.Root -> root()
      is MediaId.Book -> {
        bookRepository.get(mediaId.id)?.let(::mediaItem)
      }
      is MediaId.Chapter -> {
        val content = contentRepo.get(mediaId.bookId) ?: return null
        chapterRepo.get(mediaId.chapterId)?.let {
          mediaItem(it, content)
        }
      }
      is MediaId.ChapterMark -> {
        val content = contentRepo.get(mediaId.bookId) ?: return null
        val chapter = chapterRepo.get(mediaId.chapterId) ?: return null
        val mark = chapter.chapterMarks.getOrNull(mediaId.markIndex) ?: return null
        mediaItem(
          playbackItem = PlaybackItem(
            index = 0,
            bookId = mediaId.bookId,
            chapter = chapter,
            markIndex = mediaId.markIndex,
            mark = mark,
          ),
          content = content,
        )
      }
      MediaId.Recent -> recent()
    }
  }

  fun mediaItemsWithStartPosition(book: Book): MediaItemsWithStartPosition {
    return MediaItemsWithStartPosition(
      listOf(mediaItem(book)),
      C.INDEX_UNSET,
      C.TIME_UNSET,
    )
  }

  suspend fun mediaItemsWithStartPosition(id: String): MediaItemsWithStartPosition? {
    return when (val mediaId = id.toMediaIdOrNull()) {
      is MediaId.Book -> {
        val book = bookRepository.get(mediaId.id) ?: return null
        mediaItemsWithStartPosition(book)
      }
      is MediaId.Chapter, is MediaId.ChapterMark, MediaId.Root, MediaId.Recent, null -> null
    }
  }

  suspend fun chapters(bookId: BookId): List<MediaItem>? {
    val book = bookRepository.get(bookId) ?: return null
    return playbackItems(book)
  }

  internal fun playbackItems(book: Book): List<MediaItem> {
    return book.playbackItems().map { playbackItem ->
      mediaItem(playbackItem, book.content)
    }
  }

  suspend fun children(id: String): List<MediaItem>? {
    val mediaId = id.toMediaIdOrNull() ?: return null
    return when (mediaId) {
      MediaId.Root -> {
        bookRepository.all()
          .sortedWith(BookComparator.ByLastPlayed)
          .map { book ->
            mediaItem(book)
          }
      }
      is MediaId.Book -> chapters(mediaId.id)
      is MediaId.Chapter, is MediaId.ChapterMark -> null
      MediaId.Recent -> {
        val bookId = currentBookStoreId.data.first() ?: return null
        val book = bookRepository.get(bookId) ?: return null
        listOf(mediaItem(book))
      }
    }
  }

  fun mediaItem(book: Book): MediaItem = MediaItem(
    title = book.content.name,
    mediaId = MediaId.Book(book.id),
    browsable = false,
    isPlayable = true,
    mediaType = MediaType.AudioBook,
  )

  private fun mediaItem(
    chapter: Chapter,
    content: BookContent,
  ) = MediaItem(
    title = chapter.name ?: chapter.id.value,
    mediaId = MediaId.Chapter(bookId = content.id, chapterId = chapter.id),
    browsable = false,
    isPlayable = true,
    sourceUri = chapter.id.toUri(),
    artist = content.author,
    mediaType = MediaType.AudioBookChapter,
  )

  private fun mediaItem(
    playbackItem: PlaybackItem,
    content: BookContent,
  ) = MediaItem(
    title = playbackItem.mark.name
      ?: playbackItem.chapter.name
      ?: playbackItem.chapter.id.value,
    mediaId = playbackItem.mediaId,
    browsable = false,
    isPlayable = true,
    sourceUri = playbackItem.chapter.id.toUri(),
    artist = content.author,
    durationMs = playbackItem.mark.durationMs,
    clippingConfiguration = ClippingConfiguration.Builder()
      .setStartPositionMs(playbackItem.mark.startMs)
      .setEndPositionMs(playbackItem.mark.endMs)
      .build(),
    mediaType = MediaType.AudioBookChapter,
  )
}
