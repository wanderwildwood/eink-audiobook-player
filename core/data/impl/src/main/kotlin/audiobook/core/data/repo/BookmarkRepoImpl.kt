package audiobook.core.data.repo

import androidx.room.RoomDatabase
import audiobook.core.data.Book
import audiobook.core.data.BookContent
import audiobook.core.data.Bookmark
import audiobook.core.data.repo.internals.dao.BookmarkDao
import audiobook.core.data.repo.internals.transaction
import audiobook.core.data.runForMaxSqlVariableNumber
import audiobook.core.logging.api.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

@ContributesBinding(AppScope::class)
public class BookmarkRepoImpl
internal constructor(
  private val dao: BookmarkDao,
  private val appDb: RoomDatabase,
) : BookmarkRepo {

  override suspend fun deleteBookmark(id: Bookmark.Id) {
    dao.deleteBookmark(id)
  }

  override suspend fun addBookmark(bookmark: Bookmark) {
    dao.addBookmark(bookmark)
  }

  override suspend fun addBookmarkAtBookPosition(
    book: Book,
    title: String?,
    setBySleepTimer: Boolean,
  ): Bookmark {
    return withContext(Dispatchers.IO) {
      val bookMark = Bookmark(
        title = title,
        time = book.content.positionInChapter,
        id = Bookmark.Id.random(),
        addedAt = Instant.now(),
        setBySleepTimer = setBySleepTimer,
        chapterId = book.content.currentChapter,
        bookId = book.id,
      )
      addBookmark(bookMark)
      Logger.v("Added bookmark=$bookMark")
      bookMark
    }
  }

  override suspend fun bookmarks(book: BookContent): List<Bookmark> {
    val chapters = book.chapters
    return appDb.transaction {
      chapters.runForMaxSqlVariableNumber {
        dao.allForChapters(it)
      }
    }
  }
}
