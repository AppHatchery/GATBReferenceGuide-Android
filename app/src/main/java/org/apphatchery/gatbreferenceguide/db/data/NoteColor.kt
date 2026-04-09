// Lightweight value holder for a single note background-colour choice in the colour-picker UI.
// NoteColor instances are built from a hardcoded palette list in the ViewModel or Fragment and
// displayed in the colour-picker RecyclerView when the user creates or edits a NoteEntity.
//
// The colour is stored as a hex string (e.g. "#FFF176") matching NoteEntity.noteColor, so the
// adapter can mark the currently selected colour by comparing NoteColor.color to the active note's
// noteColor field without any conversion.
//
// Related: NoteEntity.noteColor, NoteDao, FABodyFragment (note creation UI).
package org.apphatchery.gatbreferenceguide.db.data

data class NoteColor(
    val color: String
)
