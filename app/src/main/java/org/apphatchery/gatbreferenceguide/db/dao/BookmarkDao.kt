package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity

@Dao
interface BookmarkDao {

    @Insert
    suspend fun insert(data: BookmarkEntity)

    @Delete
    suspend fun delete(data: BookmarkEntity)

    @Query("SELECT  * FROM  BookmarkEntity WHERE bookmarkTitle LIKE '%' || :keyword || '%'  ORDER BY bookmarkId ")
    fun getBookmarkEntity(keyword: String = ""): Flow<List<BookmarkEntity>>


    @Query("SELECT  * FROM  BookmarkEntity WHERE subChapterId=:id")
    fun getBookmarkBySubChapterId(id: Int): Flow<BookmarkEntity>

}