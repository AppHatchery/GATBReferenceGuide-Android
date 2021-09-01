package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val chapterId: Int = 0,
    val chapterTitle: String
) : Parcelable
