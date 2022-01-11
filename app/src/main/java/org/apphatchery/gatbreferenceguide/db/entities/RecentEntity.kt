package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val timeStamp: Long = System.currentTimeMillis()
)