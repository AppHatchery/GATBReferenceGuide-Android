package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.NoteTargetRow
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: NoteEntity)

    @Delete
    suspend fun delete(data: NoteEntity)


    @Query("SELECT  * FROM  NoteEntity WHERE noteId=:id  ORDER BY lastEdit DESC")
    fun getNoteById(id: String): Flow<List<NoteEntity>>


    @Query("SELECT  * FROM  NoteEntity  ORDER BY lastEdit DESC")
    fun getNoteEntity(): Flow<List<NoteEntity>>

    @Update
    suspend fun update(data: NoteEntity)

    @Query("SELECT noteIdPrimaryKey, noteId, subChapterId FROM NoteEntity")
    suspend fun getAllNoteTargets(): List<NoteTargetRow>

    @Query("UPDATE NoteEntity SET noteId = :newNoteId WHERE noteIdPrimaryKey = :pk")
    suspend fun updateNoteTargetByPk(pk: Int, newNoteId: String): Int

    @Query("UPDATE NoteEntity SET noteId = :newNoteId, subChapterId = :newSubChapterId WHERE noteIdPrimaryKey = :pk")
    suspend fun updateNoteTargetAndSubChapterByPk(pk: Int, newNoteId: String, newSubChapterId: Int): Int

    @Query("DELETE FROM NoteEntity")
    suspend fun clearNotes()
}