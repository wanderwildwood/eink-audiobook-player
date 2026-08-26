package audiobook.core.data.repo

import audiobook.core.data.Book
import audiobook.core.data.BookContent
import audiobook.core.data.BookId
import kotlinx.coroutines.flow.Flow

public interface BookRepository {

  public fun flow(): Flow<List<Book>>

  public suspend fun all(): List<Book>

  public fun flow(id: BookId): Flow<Book?>

  public suspend fun get(id: BookId): Book?

  public suspend fun updateBook(
    id: BookId,
    update: (BookContent) -> BookContent,
  )
}
