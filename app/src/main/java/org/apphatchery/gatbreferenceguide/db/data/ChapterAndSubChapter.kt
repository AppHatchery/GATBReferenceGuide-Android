// Room relation object joining ChapterEntity with its list of SubChapterEntity children.
// Used by ChapterDao to load an entire chapter and all its pages in a single query, which is
// then consumed by the chapter-detail screen to populate its RecyclerView of sub-chapter entries.
//
// Why this join is needed: ChapterEntity holds only the chapter title and position; the individual
// content pages live in SubChapterEntity keyed by chapterId. Fetching them together avoids a
// second DAO call in the ViewModel and lets Room manage the 1-to-many mapping automatically.
//
// @Embedded flattens ChapterEntity columns directly into the query result row.
// @Relation instructs Room to run a follow-up SELECT on SubChapterEntity WHERE chapterId matches,
//   returning the full list — this is NOT a SQL JOIN but a two-query batch managed by Room.
//
// Implements Parcelable so the object can travel in navigation Bundle arguments.
// Related: ChapterEntity, SubChapterEntity, ChapterDao, FAChapterViewModel, ChapterFragment.
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
