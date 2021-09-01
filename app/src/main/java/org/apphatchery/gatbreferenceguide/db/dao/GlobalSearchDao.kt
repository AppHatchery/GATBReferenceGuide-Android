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


    @Query("SELECT  * FROM  GlobalSearchEntity  WHERE  subChapter LIKE '%' || :keyword || '%'  OR subChapter LIKE '%' || :keyword || '%' OR searchTitle LIKE '%' || :keyword || '%'  OR textInBody LIKE '%' || :keyword || '%' ORDER BY chapterId")
    fun getGlobalSearchEntity(keyword: String = ""): Flow<List<GlobalSearchEntity>>

}
