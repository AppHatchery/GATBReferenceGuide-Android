package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity

@Dao
interface ChartDao {

    @Insert
    suspend fun insert(data: List<ChartEntity>)

    @Delete
    suspend fun delete(data: ChartEntity)

    @Transaction
    @Query("SELECT  * FROM  ChartEntity  JOIN SubChapterEntity USING(subChapterTitle)")
    fun getChartAndSubChapter(): Flow<List<ChartAndSubChapter>>


    @Transaction
    @Query("SELECT  * FROM  ChartEntity JOIN SubChapterEntity USING(subChapterTitle)")
    suspend fun getChartAndSubChapterSuspend(): List<ChartAndSubChapter>

}