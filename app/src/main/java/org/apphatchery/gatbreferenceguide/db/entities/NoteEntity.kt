// Room entity for the Notes table — stores clinician-authored notes tied to specific guide pages.
//
// Table name: NoteEntity (Room default).
// Primary key: noteIdPrimaryKey — auto-generated integer rowid; used internally by NoteDao to
//   update individual rows without touching the user-facing noteId string key.
//
// Fields:
//   noteIdPrimaryKey — auto-increment PK; used by updateNoteTargetByPk() during content migrations
//                      so the user's note text is preserved even when sub-chapter IDs are renumbered.
//   noteId           — String version of the associated sub-chapter/chart ID; mirrors BookmarkEntity's
//                      bookmarkId convention ("table_*" for charts, numeric string for sub-chapters).
//   noteText         — the actual note content written by the user.
//   noteTitle        — short title for the note, shown in the Saved screen list.
//   noteColor        — hex colour string (e.g. "#FFF176") chosen by the user in NoteColor picker.
//   subChapterId     — numeric FK into SubChapterEntity; kept in sync with noteId during migrations.
//   lastEdit         — epoch-millisecond timestamp updated on every save; drives DESC sort order.
//
// User data — never auto-wiped. lastEditDateFormat formats lastEdit for display in the notes list.
// Related: NoteDao, NoteTargetRow, NoteColor, FASavedViewModel, LegacyNotesMigrator, FABodyFragment.
package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val noteIdPrimaryKey: Int = 0,
    val noteId: String = 0.toString(),
    val noteText: String,
    val noteTitle: String,
    val noteColor: String,
    val subChapterId: Int,
    val lastEdit: Long = System.currentTimeMillis()
) {
    val lastEditDateFormat: String
        get() =
            SimpleDateFormat("MM/dd/yyyy", Locale.US).format(lastEdit)
}
