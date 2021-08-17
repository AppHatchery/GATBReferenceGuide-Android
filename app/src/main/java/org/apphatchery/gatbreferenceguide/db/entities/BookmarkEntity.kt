package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val bookmarkId: Int = 0,
    val subChapterId: Int = 0,
    val chapterId: Int = 0,
    val bookmarkTitle: String ="",
    val isSubChapter: Boolean = true,
)