// DAO for ChartEntity — the diagnostic/treatment charts in the GA-TB Reference Guide.
// Charts are nested under SubChapterEntities; each ChartEntity carries a subChapterId foreign key
// that this DAO joins against to produce ChartAndSubChapter results consumed by FAChartViewModel.
//
// All read queries are @Transaction to prevent inconsistent reads across the two tables involved.
// Two variants of the full-list query exist: getChartAndSubChapter() returns a Flow (observed live
// by FAChartViewModel.getChart in ChartFragment) while getChartAndSubChapterSuspend() is a one-shot
// suspend for use during the startup reseed pipeline where a Flow subscriber would be inappropriate.
//
// getChartAndSubChapterByIdOrNull(): adds LIMIT 1 and a nullable return so callers (e.g.
// FASavedViewModel) can distinguish "chart exists" from "orphaned bookmark" after a content update.
//
// insert() uses IGNORE strategy — duplicate chart IDs during a reseed are silently dropped rather
// than overwriting, so deleteAll() must be called first to force a full refresh.
//
// Related: ChartEntity, ChartAndSubChapter, FAChartViewModel, FASavedViewModel, SubChapterDao.
package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity

@Dao
interface ChartDao {

 /** Inserts a batch of charts; silently skips duplicates (IGNORE). Call deleteAll() first
  *  when doing a full reseed so stale charts from a previous content version are removed. */
 @Insert(onConflict = OnConflictStrategy.IGNORE)
 suspend fun insert(data: List<ChartEntity>)

 @Delete
 suspend fun delete(data: ChartEntity)

 /** Live-observable list of all charts with their parent subchapter, consumed by FAChartViewModel.
  *  Use the suspend variant below during startup seeding where a long-lived Flow is undesirable. */
 @Transaction
 @Query(
     "SELECT * FROM ChartEntity " +
     "INNER JOIN SubChapterEntity " +
     "ON ChartEntity.subChapterId = SubChapterEntity.subChapterId"
 )
 fun getChartAndSubChapter(): Flow<List<ChartAndSubChapter>>

 /** One-shot suspend read of all charts; used by the reseed pipeline to compare existing rows
  *  before deciding whether to wipe and re-insert the chart catalogue on first launch. */
 @Transaction
 @Query(
     "SELECT * FROM ChartEntity " +
     "INNER JOIN SubChapterEntity " +
     "ON ChartEntity.subChapterId = SubChapterEntity.subChapterId"
 )
 suspend fun getChartAndSubChapterSuspend(): List<ChartAndSubChapter>

 /** Observed by FASavedViewModel to resolve a bookmarked chart's parent subchapter for navigation.
  *  Non-nullable — assumes the chart ID is valid; prefer getChartAndSubChapterByIdOrNull for
  *  bookmarks that may reference content removed in a guide update. */
 @Transaction
 @Query(
     "SELECT * FROM ChartEntity " +
     "INNER JOIN SubChapterEntity " +
     "ON ChartEntity.subChapterId = SubChapterEntity.subChapterId " +
     "WHERE ChartEntity.id = :id"
 )
 fun getChartAndSubChapterById(id: String): Flow<ChartAndSubChapter>

 /** Nullable variant used by FASavedViewModel to detect orphaned chart bookmarks after a content
  *  update. LIMIT 1 prevents Room from emitting multiple rows if the join ever produces duplicates. */
 @Transaction
 @Query(
     "SELECT * FROM ChartEntity " +
     "INNER JOIN SubChapterEntity " +
     "ON ChartEntity.subChapterId = SubChapterEntity.subChapterId " +
     "WHERE ChartEntity.id = :id LIMIT 1"
 )
 fun getChartAndSubChapterByIdOrNull(id: String): Flow<ChartAndSubChapter?>

 @Query("DELETE FROM ChartEntity")
 suspend fun deleteAll()

 @Query("SELECT COUNT(*) FROM ChartEntity")
 suspend fun count(): Int

}