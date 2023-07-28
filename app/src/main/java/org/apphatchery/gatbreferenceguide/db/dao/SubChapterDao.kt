package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.ChapterAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

@Dao
interface SubChapterDao {
    @Query("DELETE FROM SubChapterEntity")
    suspend fun deleteAll()
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: List<SubChapterEntity>)

    @Query("SELECT  * FROM  SubChapterEntity WHERE subChapterTitle LIKE '%' || :keyword || '%'  ORDER BY subChapterId")
    fun getSubChapter(keyword: String = ""): Flow<List<SubChapterEntity>>

    @Query("SELECT  * FROM  SubChapterEntity WHERE chapterId=:chapterId AND  subChapterTitle LIKE '%' || :keyword || '%'  ORDER BY subChapterId")
    fun getSubChapterSearch(chapterId: Int, keyword: String = ""): Flow<List<SubChapterEntity>>

    @Query("SELECT COUNT(*) FROM SubChapterEntity WHERE chapterId=:chapterId")
    fun getCountByChapterId(chapterId: Int): Flow<Int>

    @Transaction
    @Query("SELECT * FROM ChapterEntity")
    fun getSubChapterBindChapter(): Flow<List<ChapterAndSubChapter>>


    @Transaction
    @Query("SELECT * FROM ChapterEntity WHERE chapterId=:id")
    fun getSubChapterBindChapterByChapterId(id: Int): Flow<ChapterAndSubChapter>

    @Transaction
    @Query("SELECT * FROM ChapterEntity  ORDER BY chapterId")
    suspend fun getSubChapterBindChapterSuspended(): List<ChapterAndSubChapter>

    @Query("SELECT  * FROM  SubChapterEntity WHERE subChapterId=:id OR subChapterTitle=:id")
    fun getSubChapterById(id: String): Flow<SubChapterEntity>


    fun getSubChapterEntity(
        keyword: String = "",
        chapterId: Int = 0,
        isSearchBindChapter: Boolean = false
    ): Flow<List<SubChapterEntity>> {
        return if (isSearchBindChapter) getSubChapterSearch(chapterId, keyword) else getSubChapter(
            keyword
        )
    }
}