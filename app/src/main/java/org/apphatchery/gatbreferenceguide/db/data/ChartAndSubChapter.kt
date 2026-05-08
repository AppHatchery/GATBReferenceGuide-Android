package org.apphatchery.gatbreferenceguide.db.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

@Parcelize
data class ChartAndSubChapter(
@Embedded val chartEntity: ChartEntity,
@Relation(
parentColumn = "subChapterId",
entityColumn = "subChapterId"
)
val subChapterEntity: SubChapterEntity
) : Parcelable