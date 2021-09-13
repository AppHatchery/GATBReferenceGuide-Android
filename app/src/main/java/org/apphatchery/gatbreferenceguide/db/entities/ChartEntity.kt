package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class ChartEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val chartTitle: String,
    val subChapterTitle: String,
    val chartHomePosition: Int
) : Parcelable
