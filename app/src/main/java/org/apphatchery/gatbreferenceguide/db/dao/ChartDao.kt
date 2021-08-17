package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity

@Dao
interface ChartDao {

    @Insert
    suspend fun insert(data: List<ChartEntity>)

    @Delete
    suspend fun delete(data: ChartEntity)


    @Query("SELECT  * FROM  ChartEntity")
    fun getChartEntityEntity(): Flow<List<ChartEntity>>

}