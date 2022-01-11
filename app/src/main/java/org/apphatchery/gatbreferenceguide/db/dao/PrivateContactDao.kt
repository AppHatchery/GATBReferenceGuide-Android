package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.db.entities.PrivateContact

@Dao
interface PrivateContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: PrivateContact)

    @Query("SELECT  * FROM  PrivateContact ORDER BY fullName ASC")
    fun getContacts(): Flow<List<PrivateContact>>

    @Update
    suspend fun update(data:PrivateContact)

    @Delete
    suspend fun delete(data: PrivateContact)

    @Query("DELETE FROM PrivateContact")
    suspend fun clearContact()
}