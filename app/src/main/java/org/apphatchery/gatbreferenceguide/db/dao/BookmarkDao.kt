package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.enums.BookmarkType

@Dao
interface BookmarkDao {

    @Insert
    suspend fun insert(data: BookmarkEntity)

    @Delete
    suspend fun delete(data: BookmarkEntity)

    @Update
    suspend fun update(data: BookmarkEntity)

    @Query("SELECT  * FROM  BookmarkEntity WHERE bookmarkTitle LIKE '%' || :keyword || '%'  ORDER BY bookmarkId ")
    fun getBookmarkEntity(keyword: String = ""): Flow<List<BookmarkEntity>>


    @Query("SELECT  * FROM  BookmarkEntity WHERE subChapterId=:id AND chartId ='' ")
    fun getBookmarkBySubChapterId(id: String): Flow<BookmarkEntity>

    @Query("SELECT  * FROM  BookmarkEntity WHERE chartId=:id")
    fun getBookmarkByChartId(id: String): Flow<BookmarkEntity>

    fun getBookmarkById(id: String, bookmarkType: BookmarkType): Flow<BookmarkEntity> =
        when (bookmarkType) {
            BookmarkType.SUBCHAPTER -> getBookmarkBySubChapterId(id)
            BookmarkType.CHART -> getBookmarkByChartId(id)

        }

}