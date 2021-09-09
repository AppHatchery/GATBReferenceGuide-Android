package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.ChapterAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

@Dao
interface RecentDao {

    @Insert
    suspend fun insert(data: RecentEntity)

    @Delete
    suspend fun delete(data: RecentEntity)

    @Query("SELECT * FROM RecentEntity ORDER BY timeStamp DESC")
    fun getRecentEntity(): Flow<List<RecentEntity>>

}