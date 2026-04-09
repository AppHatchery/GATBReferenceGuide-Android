// DAO for all BookmarkEntity read/write operations, plus migration-safe ID redirect logic.
// Bookmarks store the user's saved guide pages using a bookmarkId that matches either a
// SubChapterEntity.subChapterId (regular pages) or a "table_*" string (chart/table pages).
//
// repairRedirect(): the most important function here — handles guide content renames between
// app versions. When a chapter/table is renumbered, any stored bookmark ID becomes stale.
// This function safely renames the PK in-place, or merges with the new row if it already exists
// (deduplication), or inserts a fresh row if the old one is missing. Called at startup from
// SavedFragment and LegacyNotesMigrator via the redirect map in LegacyRedirects.kt.
//
// getBookmarkEntity(keyword): returns a Flow so the Saved screen auto-updates live. The default
// empty keyword returns all bookmarks ordered by bookmarkId.
//
// insert() vs insertIgnore(): use insert() for new user-created bookmarks (crash on duplicate is
// intentional — it signals a coding bug). Use insertIgnore() only inside migration/repair code.
// Related: BookmarkEntity, FASavedViewModel, SavedFragment, LegacyRedirects, LegacyNotesMigrator.
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
     * One-time repair when legacy bookmarks are redirected to a new ID after a content rename.
     *
     * Room's @Update cannot change a primary key, so we must use raw SQL UPDATE.
     * Three cases are handled atomically inside a @Transaction:
     *  1. oldId == newId → only metadata fields need updating; quick-return path.
     *  2. newId already exists in the table → deduplicate by keeping the new row and deleting the
     *     old one, merging titles (prefer the provided title if non-blank).
     *  3. Normal rename → SQL UPDATE changes the PK. If the row is missing (updated == 0),
     *     insertIgnore creates a fresh row rather than leaving the user's bookmark lost.
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
