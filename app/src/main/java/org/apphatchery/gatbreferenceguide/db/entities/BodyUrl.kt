package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

@Parcelize
data class BodyUrl(
    val chapterEntity: ChapterEntity,
    val subChapterEntity: SubChapterEntity? = null,
) : Parcelable
