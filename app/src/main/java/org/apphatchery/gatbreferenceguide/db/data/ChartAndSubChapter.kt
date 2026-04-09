// Room relation object joining a ChartEntity with its single parent SubChapterEntity.
// Used by ChartDao to load a chart alongside the sub-chapter it belongs to, so the Charts screen
// can display the parent sub-chapter title and navigate to the correct HTML page when a chart is
// tapped — without requiring a separate DAO query in the ViewModel.
//
// Why this join is needed: ChartEntity stores only the chart title and a subChapterId FK; the HTML
// filename and full sub-chapter title needed for navigation live in SubChapterEntity. Loading both
// together eliminates a follow-up query and gives the UI everything it needs in one object.
//
// @Embedded flattens ChartEntity columns into the result row.
// @Relation fetches the matching SubChapterEntity by subChapterId (1-to-1 relationship).
// Note: unlike ChapterAndSubChapter, the child here is a single entity, not a List.
//
// Implements Parcelable for navigation Bundle arguments.
// Related: ChartEntity, SubChapterEntity, ChartDao, FAChartsViewModel, ChartsFragment.
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
