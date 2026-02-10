package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity

@Dao
interface BookmarkDao {

    @Insert
    suspend fun insert(data: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(data: BookmarkEntity)

    @Delete
    suspend fun delete(data: BookmarkEntity)

    @Update
    suspend fun update(data: BookmarkEntity)

    @Query("SELECT  * FROM  BookmarkEntity WHERE bookmarkTitle LIKE '%' || :keyword || '%'  ORDER BY bookmarkId ")
    fun getBookmarkEntity(keyword: String = ""): Flow<List<BookmarkEntity>>

    @Query("SELECT  * FROM  BookmarkEntity WHERE bookmarkId=:id")
    fun getBookmarkById(id: String): Flow<BookmarkEntity>

    @Query("SELECT * FROM BookmarkEntity WHERE bookmarkId=:id LIMIT 1")
    suspend fun getBookmarkByIdOnceOrNull(id: String): BookmarkEntity?

    @Query("UPDATE BookmarkEntity SET bookmarkTitle=:title, subChapter=:subChapter WHERE bookmarkId=:id")
    suspend fun updateBookmarkFields(id: String, title: String, subChapter: String): Int

    @Query(
        "UPDATE BookmarkEntity SET bookmarkId=:newId, bookmarkTitle=:title, subChapter=:subChapter " +
            "WHERE bookmarkId=:oldId"
    )
    suspend fun updateBookmarkIdAndFields(oldId: String, newId: String, title: String, subChapter: String): Int

    @Query("DELETE FROM BookmarkEntity WHERE bookmarkId=:id")
    suspend fun deleteById(id: String): Int

    /**
     * One-time repair when legacy bookmarks are redirected.
     *
     * - If the PK (bookmarkId) changes, @Update() can't update the original row.
     * - This performs an in-place SQL UPDATE of the PK when possible.
     * - If the redirected ID already exists, it merges and deletes the old row (dedupe).
     */
    @Transaction
    suspend fun repairRedirect(
        oldId: String,
        newId: String,
        newTitle: String,
        newSubChapter: String,
    ) {
        if (oldId == newId) {
            updateBookmarkFields(oldId, newTitle, newSubChapter)
            return
        }

        val target = getBookmarkByIdOnceOrNull(newId)
        if (target != null) {
            // Dedupe: keep the redirected row, and delete the legacy row.
            // Prefer the provided title if non-blank.
            val mergedTitle = if (newTitle.isNotBlank()) newTitle else target.bookmarkTitle
            val mergedSubChapter = if (newSubChapter.isNotBlank()) newSubChapter else target.subChapter
            updateBookmarkFields(newId, mergedTitle, mergedSubChapter)
            deleteById(oldId)
            return
        }

        val updated = updateBookmarkIdAndFields(oldId, newId, newTitle, newSubChapter)
        if (updated == 0) {
            // Nothing to update (row missing). Avoid creating duplicates by ignoring conflicts.
            insertIgnore(BookmarkEntity(bookmarkId = newId, bookmarkTitle = newTitle, subChapter = newSubChapter))
        }
    }

    @Query("DELETE FROM BookmarkEntity")
    suspend fun clearBookmarks()

}