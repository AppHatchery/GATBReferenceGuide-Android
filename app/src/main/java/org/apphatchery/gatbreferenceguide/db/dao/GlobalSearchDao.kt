package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.GlobalSearchWithMatchInfo
import org.apphatchery.gatbreferenceguide.db.entities.FullTextSearchGlobalSearchEntity
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity

@Dao
interface GlobalSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<GlobalSearchEntity>)


    @Query(
        """SELECT  * , matchinfo(FullTextSearchGlobalSearchEntity) as matchInfo FROM 
        GlobalSearchEntity JOIN FullTextSearchGlobalSearchEntity USING(fileName) 
        WHERE FullTextSearchGlobalSearchEntity MATCH :keyword ORDER BY GlobalSearchEntity.chapterId ASC, GlobalSearchEntity.subChapterId ASC"""
    )
    fun getGlobalSearchEntity(keyword: String): Flow<List<GlobalSearchWithMatchInfo>>

}
