package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentEntity(
    @PrimaryKey(autoGenerate = false)
    val subChapterId: Int,
    val subChapterTitle: String,
    val timeStamp: Long = System.currentTimeMillis()
)