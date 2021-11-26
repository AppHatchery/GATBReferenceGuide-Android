package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity

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

    @Query("SELECT  * FROM  BookmarkEntity WHERE bookmarkId=:id")
    fun getBookmarkById(id: String): Flow<BookmarkEntity>

    @Query("DELETE FROM BookmarkEntity")
    suspend fun clearBookmarks()

}