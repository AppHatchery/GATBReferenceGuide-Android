package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.ChapterAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

@Dao
interface SubChapterDao {

    @Insert
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
     @Query("SELECT * FROM ChapterEntity  ORDER BY chapterId")
    suspend fun getSubChapterBindChapterSuspended(): List<ChapterAndSubChapter>



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