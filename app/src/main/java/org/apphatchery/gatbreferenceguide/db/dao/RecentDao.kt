package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity

@Dao
interface RecentDao {

    @Insert
    suspend fun insert(data: RecentEntity)

    @Delete
    suspend fun delete(data: RecentEntity)

    @Query("SELECT * FROM RecentEntity ORDER BY timeStamp DESC")
    fun getRecentEntity(): Flow<List<RecentEntity>>

    @Query("DELETE FROM RecentEntity")
    suspend fun clearRecent()

}