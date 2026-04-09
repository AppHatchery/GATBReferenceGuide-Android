// DAO for ChapterEntity — the top-level chapter list of the GA-TB Reference Guide.
// Each chapter groups multiple SubChapterEntities and is displayed with a Roman numeral in
// ChapterFragment via FAChapterAdapter. There are ~18 chapters in the current guide edition.
//
// getChapterEntity(keyword): the ORDER BY is non-trivial. Chapter ID 18 is a special unnumbered
// chapter (e.g. Abbreviations) that must always appear FIRST regardless of its numeric ID.
// The CASE expression achieves this: ID 18 → sort key 0, everything else → sort key 1, then
// secondary sort by chapterId. FAChapterAdapter mirrors this by skipping the Roman numeral prefix
// for chapter 18. If the unnumbered chapter's ID ever changes, update both here and the adapter.
//
// getChapterById(): returns a Flow observed by FAChartViewModel to resolve a chart's parent
// chapter entity before navigating to BodyFragment with a fully-populated BodyUrl.
// getChapterByIdOrNull(): same but emits null when no match — use for safe optional lookups.
//
// insert() uses IGNORE conflict strategy so re-seeding the same content on app update is safe.
// deleteAll() is called by Database.purgeData() before fresh content is re-seeded from assets.
// Related: ChapterEntity, ChapterFragment, FAChapterViewModel, FAChartViewModel, Repository.kt.
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

    @Query("SELECT COUNT(*) FROM ChapterEntity")
    suspend fun count(): Int
}
