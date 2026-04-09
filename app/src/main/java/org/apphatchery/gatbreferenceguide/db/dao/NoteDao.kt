// DAO for NoteEntity — user-authored notes attached to guide pages in the TB reference app.
// Notes are keyed by noteId (a string matching a chart/subchapter ID) and subChapterId (Int).
// Both fields can become stale when guide content is reorganised; LegacyNotesMigrator uses
// getAllNoteTargets() + updateNoteTargetByPk() / updateNoteTargetAndSubChapterByPk() to remap
// these IDs at startup so notes survive content restructuring across app versions.
//
// Why update by noteIdPrimaryKey (not noteId)? Multiple notes can share the same noteId (a user
// can annotate the same section several times). Keying updates on the auto-generated primary key
// ensures each row is patched individually without accidentally overwriting sibling notes.
//
// insert() uses REPLACE so editing an existing note (same noteIdPrimaryKey) overwrites the old
// row cleanly; NoteEntity.noteIdPrimaryKey defaults to 0 meaning new notes get a fresh auto ID.
//
// getNoteById(id): filtered by noteId, ordered by lastEdit DESC — consumed by FABodyViewModel
// so BodyFragment shows the most recently edited note for the current page at the top.
//
// Related: NoteEntity, NoteTargetRow, LegacyNotesMigrator, FABodyViewModel, FASavedViewModel.
package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.data.NoteTargetRow
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity

@Dao
interface NoteDao {

    /** Inserts or replaces a note. REPLACE on noteIdPrimaryKey means editing an existing note
     *  (pass the same noteIdPrimaryKey) cleanly overwrites it; passing pk=0 creates a new row. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: NoteEntity)

    @Delete
    suspend fun delete(data: NoteEntity)

    /** Notes for a specific guide page, newest-first. Observed by FABodyViewModel so BodyFragment
     *  always shows the most recently edited note for the currently open section. */
    @Query("SELECT  * FROM  NoteEntity WHERE noteId=:id  ORDER BY lastEdit DESC")
    fun getNoteById(id: String): Flow<List<NoteEntity>>

    /** All notes across every guide page, newest-first. Observed by FASavedViewModel to populate
     *  the Notes tab in SavedFragment. */
    @Query("SELECT  * FROM  NoteEntity  ORDER BY lastEdit DESC")
    fun getNoteEntity(): Flow<List<NoteEntity>>

    @Update
    suspend fun update(data: NoteEntity)

    /** Lightweight projection used exclusively by LegacyNotesMigrator to scan all note targets
     *  without loading the full noteText, keeping migration memory usage low. */
    @Query("SELECT noteIdPrimaryKey, noteId, subChapterId FROM NoteEntity")
    suspend fun getAllNoteTargets(): List<NoteTargetRow>

    /** Remaps a chart/table note's target ID after content reorganisation. Updates only noteId
     *  (not subChapterId) because chart-type notes are keyed differently from subchapter notes. */
    @Query("UPDATE NoteEntity SET noteId = :newNoteId WHERE noteIdPrimaryKey = :pk")
    suspend fun updateNoteTargetByPk(pk: Int, newNoteId: String): Int

    /** Remaps a subchapter note's target: updates both noteId and the subChapterId foreign key so
     *  the note remains discoverable by getNoteById() after a section is renamed or merged. */
    @Query("UPDATE NoteEntity SET noteId = :newNoteId, subChapterId = :newSubChapterId WHERE noteIdPrimaryKey = :pk")
    suspend fun updateNoteTargetAndSubChapterByPk(pk: Int, newNoteId: String, newSubChapterId: Int): Int

    @Query("DELETE FROM NoteEntity")
    suspend fun clearNotes()
}