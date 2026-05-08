package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity

@Dao
interface ChartDao {

 @Insert(onConflict = OnConflictStrategy.IGNORE)
 suspend fun insert(data: List<ChartEntity>)

 @Delete
 suspend fun delete(data: ChartEntity)

 @Transaction
 @Query(
     "SELECT * FROM ChartEntity " +
     "INNER JOIN SubChapterEntity " +
     "ON ChartEntity.subChapterId = SubChapterEntity.subChapterId"
 )
 fun getChartAndSubChapter(): Flow<List<ChartAndSubChapter>>

 @Transaction
 @Query(
     "SELECT * FROM ChartEntity " +
     "INNER JOIN SubChapterEntity " +
     "ON ChartEntity.subChapterId = SubChapterEntity.subChapterId"
 )
 suspend fun getChartAndSubChapterSuspend(): List<ChartAndSubChapter>

 @Transaction
 @Query(
     "SELECT * FROM ChartEntity " +
     "INNER JOIN SubChapterEntity " +
     "ON ChartEntity.subChapterId = SubChapterEntity.subChapterId " +
     "WHERE ChartEntity.id = :id"
 )
 fun getChartAndSubChapterById(id: String): Flow<ChartAndSubChapter>

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