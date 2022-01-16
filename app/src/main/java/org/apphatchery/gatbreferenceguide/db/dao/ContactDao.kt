package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.Contact

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: List<Contact>)

    @Query("SELECT  * FROM  Contact ORDER BY fullName ASC")
    fun getContacts(): Flow<List<Contact>>

    @Delete
    suspend fun delete(data: Contact)

    @Query("DELETE FROM Contact")
    suspend fun clearContact()
}