package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat

@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val noteId: Int = 0,
     val noteText: String,
    val noteColor: String,
    val subChapterId: Int ,
    val lastEdit: Long = System.currentTimeMillis()
) {
    val lastEditDateFormat: String
        get() = DateFormat.getDateTimeInstance().format(lastEdit)
}
