package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChartEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val subChapterId: Int,
    val subChapterText: String
)
