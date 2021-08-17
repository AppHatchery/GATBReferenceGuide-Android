package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class ChapterEntity(
     @PrimaryKey(autoGenerate = false)
    val chapterId: Int,
    val chapterTitle: String
) : Parcelable
