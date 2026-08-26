package audiobook.core.data.repo

import audiobook.core.data.Book
import audiobook.core.data.BookContent
import audiobook.core.data.Bookmark

public interface BookmarkRepo {
  public suspend fun deleteBookmark(id: Bookmark.Id)

  public suspend fun addBookmark(bookmark: Bookmark)

  @IgnorableReturnValue
  public suspend fun addBookmarkAtBookPosition(
    book: Book,
    title: String?,
    setBySleepTimer: Boolean,
  ): Bookmark

  public suspend fun bookmarks(book: BookContent): List<Bookmark>
}
