package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class ChapterEntity(
    @PrimaryKey(autoGenerate = false)
    val chapterId: Int = 0,
    val chapterTitle: String,
    val chapterHomePosition:Int = 0,
) : Parcelable
