package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = false)
    val bookmarkId: String = 0.toString(),
    val bookmarkTitle: String = "",
    val subChapter: String = ""
)