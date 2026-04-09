// DAO for RecentEntity — the "recently viewed" history list shown in SavedFragment of the TB app.
// Each row holds a guide page ID, its display title, and a timeStamp for ordering.
//
// insert() intentionally has NO conflict strategy (defaults to ABORT). This is because callers
// always call delete() before insert() to implement "bump to top" behaviour — see the
// FABodyViewModel.recentOpen() and FASubChapterViewModel.recentOpen() pattern. A plain REPLACE
// would update the existing row in-place, preserving its original rowid and sort position.
// Delete + insert creates a fresh row with a new rowid, which moves the entry to the top of the
// list ordered by timeStamp DESC. Do not change insert() to REPLACE without updating both callers.
//
// getRecentEntity(): ordered by timeStamp DESC, emits a Flow observed by FASavedViewModel to
// populate the Recents tab in SavedFragment. The list is not capped at the DAO level — if a
// maximum history size is needed in future, add a LIMIT clause here.
//
// Related: RecentEntity, FABodyViewModel, FASubChapterViewModel, FASavedViewModel, SavedFragment.
package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity

@Dao
interface RecentDao {

    /** No conflict strategy — callers must delete() the row first so this insert creates a fresh
     *  rowid, moving the entry to the top of the history list. See FABodyViewModel.recentOpen(). */
    @Insert
    suspend fun insert(data: RecentEntity)

    @Delete
    suspend fun delete(data: RecentEntity)

    /** Emits the full history ordered newest-first. Observed by FASavedViewModel for SavedFragment.
     *  There is no size cap at the DAO level; add LIMIT here if a maximum history size is required. */
    @Query("SELECT * FROM RecentEntity ORDER BY timeStamp DESC")
    fun getRecentEntity(): Flow<List<RecentEntity>>

    @Query("DELETE FROM RecentEntity")
    suspend fun clearRecent()

}