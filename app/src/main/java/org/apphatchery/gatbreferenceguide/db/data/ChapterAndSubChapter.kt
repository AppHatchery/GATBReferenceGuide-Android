package org.apphatchery.gatbreferenceguide.db.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

@Parcelize
data class ChapterAndSubChapter(
    @Embedded val chapterEntity: ChapterEntity,
    @Relation(
        parentColumn = "chapterId",
        entityColumn = "chapterId"
    )
    val subChapterEntity: List<SubChapterEntity>
) : Parcelable
