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
