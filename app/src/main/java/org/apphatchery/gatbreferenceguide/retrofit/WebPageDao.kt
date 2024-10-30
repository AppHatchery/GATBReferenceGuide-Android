package org.apphatchery.gatbreferenceguide.retrofit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WebPageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: WebPage)

    @Query("SELECT * FROM web_pages WHERE id = :id")
    suspend fun getPageContent(id: String): String?

}