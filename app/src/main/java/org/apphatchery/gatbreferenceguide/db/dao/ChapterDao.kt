package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity

@Dao
interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(data: List<ChapterEntity>)


    @Query(
        "SELECT  * FROM  ChapterEntity " +
            "WHERE chapterTitle LIKE '%' || :keyword || '%'  " +
            "ORDER BY CASE WHEN chapterId = 18 THEN 0 ELSE 1 END, chapterId"
    )
    fun getChapterEntity(keyword: String = ""): Flow<List<ChapterEntity>>


    @Query("SELECT  * FROM  ChapterEntity WHERE chapterId =:id")
    fun getChapterById(id: Int): Flow<ChapterEntity>

    @Query("SELECT  * FROM  ChapterEntity WHERE chapterId =:id LIMIT 1")
    fun getChapterByIdOrNull(id: Int): Flow<ChapterEntity?>

    @Query("DELETE FROM ChapterEntity")
    suspend fun deleteAll()
}