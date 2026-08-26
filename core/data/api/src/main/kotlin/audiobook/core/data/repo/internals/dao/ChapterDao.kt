package audiobook.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import audiobook.core.data.Chapter
import audiobook.core.data.ChapterId

@Dao
public interface ChapterDao {

  @Query("SELECT * FROM chapters2 WHERE id = :id")
  public suspend fun chapter(id: ChapterId): Chapter?

  @Query("SELECT * FROM chapters2 WHERE id IN (:ids)")
  public suspend fun chapters(ids: List<ChapterId>): List<Chapter>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(chapter: Chapter)
}
