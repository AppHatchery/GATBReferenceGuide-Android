package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(data: NoteEntity)

    @Delete
    suspend fun delete(data: NoteEntity)


    @Query("SELECT  * FROM  NoteEntity WHERE isSubChapter=:isSubChapter AND subOrChapterId=:subOrChapterId ORDER BY noteId DESC")
    fun getNoteEntity(isSubChapter: Boolean = false, subOrChapterId: Int): Flow<List<NoteEntity>>

}