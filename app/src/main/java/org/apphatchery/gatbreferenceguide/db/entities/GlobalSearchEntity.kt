package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GlobalSearchEntity(
    val searchTitle: String,
    val subChapter: String,
    val textInBody: String,
    @PrimaryKey(autoGenerate = false)
    val fileName: String = "",
    val chapterId: Int = 0,
    val subChapterId: Int = 0,
    val isChart: Boolean = false
)

