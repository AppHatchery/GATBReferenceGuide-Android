package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.HtmlInfoEntity

@Dao
interface HtmlInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<HtmlInfoEntity>)

    @Delete
    suspend fun delete(data: HtmlInfoEntity)

    @Query("SELECT  * FROM  HtmlInfoEntity")
    fun getHtmlInfoEntity(): Flow<List<HtmlInfoEntity>>

    @Query("SELECT  * FROM  HtmlInfoEntity")
   suspend fun getHtmlInfoEntitySuspended(): List<HtmlInfoEntity>

    @Query("DELETE FROM HtmlInfoEntity")
    suspend fun deleteAll()

}