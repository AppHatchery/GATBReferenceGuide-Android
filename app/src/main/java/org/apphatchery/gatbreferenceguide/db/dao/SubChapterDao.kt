// DAO for SubChapterEntity — the individual guide sections that live under each Chapter.
// SubChapters are the primary navigation unit: each one has a URL that BodyFragment loads
// into its WebView, and a subChapterId that anchors bookmarks, notes, and recent history.
//
// getSubChapterEntity() (the non-annotated Kotlin function) is a dispatch helper: it routes to
// getSubChapterSearch() when isSearchBindChapter=true (SubChapterFragment showing one chapter's
// sections), or to the global getSubChapter() otherwise. FASubChapterViewModel drives this via
// a flatMapLatest on a MutableStateFlow<String> for live search.
//
// findSubChapterByKeyOnceOrNull(): one-shot suspend lookup that tries subChapterId, subChapterTitle,
// AND url — used by LegacyNotesMigrator and the bookmark repair path to resolve stale IDs from
// older app versions where different field types were stored as the reference key.
//
// NOTE: subChapterId 28 was removed from the content but is kept in the DB for referential
// integrity. FASubChapterViewModel filters it from the UI list. Do not delete that row from the
// seed data or bookmarks/notes targeting it will lose their foreign key anchor.
//
// Related: SubChapterEntity, ChapterAndSubChapter, FASubChapterViewModel, FABodyViewModel,
// LegacyNotesMigrator, SubChapterFragment, BodyFragment.
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

    /** Global subchapter search across all chapters, ordered by subChapterId. The default empty
     *  keyword returns the full list; used by FABodyViewModel to populate the navigation drawer. */
    @Query("SELECT  * FROM  SubChapterEntity WHERE subChapterTitle LIKE '%' || :keyword || '%'  ORDER BY subChapterId")
    fun getSubChapter(keyword: String = ""): Flow<List<SubChapterEntity>>

    /** Chapter-scoped subchapter search, used in SubChapterFragment to filter sections within one
     *  chapter. FASubChapterViewModel dispatches here via getSubChapterEntity(isSearchBindChapter=true). */
    @Query("SELECT  * FROM  SubChapterEntity WHERE chapterId=:chapterId AND  subChapterTitle LIKE '%' || :keyword || '%'  ORDER BY subChapterId")
    fun getSubChapterSearch(chapterId: Int, keyword: String = ""): Flow<List<SubChapterEntity>>

    @Query("SELECT COUNT(*) FROM SubChapterEntity WHERE chapterId=:chapterId")
    fun getCountByChapterId(chapterId: Int): Flow<Int>

    /** @Transaction prevents a torn read across ChapterEntity and its child SubChapterEntities.
     *  Used by FAChapterViewModel to display the full chapter+subchapter tree. */
    @Transaction
    @Query("SELECT * FROM ChapterEntity")
    fun getSubChapterBindChapter(): Flow<List<ChapterAndSubChapter>>

    /** Single-chapter variant of getSubChapterBindChapter(); observed by FASubChapterViewModel
     *  so SubChapterFragment can reactively update when the chapter's sections change. */
    @Transaction
    @Query("SELECT * FROM ChapterEntity WHERE chapterId=:id")
    fun getSubChapterBindChapterByChapterId(id: Int): Flow<ChapterAndSubChapter>

    /** One-shot suspend read of the full chapter+subchapter tree; used during the startup reseed
     *  to compare existing DB contents before deciding whether to wipe and re-seed. */
    @Transaction
    @Query("SELECT * FROM ChapterEntity  ORDER BY chapterId")
    suspend fun getSubChapterBindChapterSuspended(): List<ChapterAndSubChapter>

    /** Resolves a subchapter by numeric ID or title string. Matching on both fields handles cases
     *  where older code stored the title rather than the numeric ID as the reference key. */
    @Query("SELECT  * FROM  SubChapterEntity WHERE subChapterId=:id OR subChapterTitle=:id")
    fun getSubChapterById(id: String): Flow<SubChapterEntity>

    /** Nullable variant for FASavedViewModel; LIMIT 1 guards against duplicate matches.
     *  Returns null if the subchapter no longer exists after a content update. */
    @Query("SELECT  * FROM  SubChapterEntity WHERE subChapterId=:id OR subChapterTitle=:id LIMIT 1")
    fun getSubChapterByIdOrNull(id: String): Flow<SubChapterEntity?>

    /** One-shot lookup that tries subChapterId, subChapterTitle, AND url — the broadest possible
     *  key search. Used by LegacyNotesMigrator and bookmark repair to resolve stale references
     *  regardless of which field was stored as the key in older app versions. */
    @Query(
        "SELECT * FROM SubChapterEntity " +
            "WHERE subChapterId=:key OR subChapterTitle=:key OR url=:key LIMIT 1"
    )
    suspend fun findSubChapterByKeyOnceOrNull(key: String): SubChapterEntity?

    /**
     * Dispatch helper that routes to the appropriate underlying query based on scope.
     * Pass isSearchBindChapter=true with a chapterId to search within a single chapter
     * (SubChapterFragment), or false to search globally (BodyFragment nav drawer).
     */
    fun getSubChapterEntity(
        keyword: String = "",
        chapterId: Int = 0,
        isSearchBindChapter: Boolean = false
    ): Flow<List<SubChapterEntity>> {
        return if (isSearchBindChapter) getSubChapterSearch(chapterId, keyword) else getSubChapter(
            keyword
        )
    }

    @Query("SELECT COUNT(*) FROM SubChapterEntity")
    suspend fun count(): Int
}