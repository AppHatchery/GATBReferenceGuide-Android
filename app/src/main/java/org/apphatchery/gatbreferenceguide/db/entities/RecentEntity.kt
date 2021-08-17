package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentEntity(
    @PrimaryKey(autoGenerate = false)
    val chapterId: Int,
    val timeStamp: Long
)