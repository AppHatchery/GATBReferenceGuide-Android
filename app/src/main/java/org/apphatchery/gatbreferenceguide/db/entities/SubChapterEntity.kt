package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class SubChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val subChapterId: Int = 0,
    val chapterId: Int,
    val subChapterTitle: String,
    val url: String ="",
    val lastUpdated: String =""
) : Parcelable
