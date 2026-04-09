// DAO for GlobalSearchEntity — the full-text search (FTS4) index powering the app-wide search.
// Room's FTS support enables MATCH queries, which are far faster than LIKE '%keyword%' for
// scanning the entire guide text. The FTS virtual table is backed by GlobalSearchEntity.
//
// getGlobalSearchEntity(keyword): uses Room FTS MATCH syntax. The keyword goes directly into the
// MATCH clause — do NOT add wildcards or SQL operators; FTS tokenization handles that internally.
// Results are sorted by chapterId then subChapterId to present hits grouped by guide hierarchy.
// Returns a Flow so GlobalSearchFragment auto-updates as the user types in the search bar.
//
// The commented-out getChapters()/getCharts() queries previously split results by isChart flag
// (chapters vs tables). They were consolidated into a single query with client-side filtering.
// Left in comments in case separate tab views are needed in a future redesign.
//
// insert() uses REPLACE strategy because the entire FTS index is dropped and rebuilt during each
// guide content update cycle. deleteAll() is called inside Database.purgeData() before re-seeding.
// count() is used by GuideContentUpdater to check whether seeding is needed on first launch.
// Related: GlobalSearchEntity, GlobalSearchFragment, FAGlobalSearchViewModel, GuideContentUpdater.
package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity

@Dao
interface GlobalSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<GlobalSearchEntity>)

    @Query(
        """SELECT DISTINCT * FROM GlobalSearchEntity
            WHERE GlobalSearchEntity MATCH :keyword
            ORDER BY GlobalSearchEntity.chapterId ASC, GlobalSearchEntity.subChapterId ASC"""
    )
    fun getGlobalSearchEntity(keyword: String =""): Flow<List<GlobalSearchEntity>>

//    @Query(
//        """SELECT * FROM GlobalSearchEntity WHERE GlobalSearchEntity MATCH :keyword AND isChart = 0 ORDER BY GlobalSearchEntity.chapterId ASC, GlobalSearchEntity.subChapterId ASC"""
//    )
//    fun getChapters(keyword: String =""): Flow<List<GlobalSearchEntity>>
//
//    @Query(
//        """SELECT * FROM GlobalSearchEntity WHERE GlobalSearchEntity MATCH :keyword AND isChart = 1 ORDER BY GlobalSearchEntity.chapterId ASC, GlobalSearchEntity.subChapterId ASC"""
//    )
//    fun getCharts(keyword: String =""): Flow<List<GlobalSearchEntity>>

    @Query("DELETE FROM GlobalSearchEntity")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM GlobalSearchEntity")
    suspend fun count(): Int

}
