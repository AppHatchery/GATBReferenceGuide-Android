// Minimal projection of NoteEntity used exclusively by the content-migration pipeline.
// NoteDao.getAllNoteTargets() returns a list of NoteTargetRow instead of full NoteEntity objects
// to avoid loading noteText/noteTitle/noteColor blobs for every note during migration scans —
// only the three fields needed to detect and repair stale note targets are fetched.
//
// Fields:
//   noteIdPrimaryKey — the auto-generated rowid; passed to NoteDao.updateNoteTargetByPk() to
//                      repoint a note to a new sub-chapter ID without touching the note content.
//   noteId           — current String content ID (mirrors BookmarkEntity.bookmarkId convention);
//                      compared against the redirect map in LegacyRedirects to detect stale IDs.
//   subChapterId     — current numeric sub-chapter FK; updated alongside noteId when a redirect
//                      maps a sub-chapter to a new ID in a guide content update.
//
// Related: NoteEntity, NoteDao, LegacyNotesMigrator, LegacyRedirects.
package org.apphatchery.gatbreferenceguide.db.data

data class NoteTargetRow(
    val noteIdPrimaryKey: Int,
    val noteId: String,
    val subChapterId: Int,
)
