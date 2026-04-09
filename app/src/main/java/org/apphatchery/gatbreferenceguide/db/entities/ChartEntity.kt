// Room entity for the Charts table — represents a single chart or reference table embedded in the
// GA-TB guide (e.g. "Table 4.1 – First-Line Anti-TB Drug Dosages").
//
// Table name: ChartEntity (Room default).
// Primary key: id — a String key (e.g. "table_4_1") assigned at seeding time; autoGenerate = false.
//   The "table_*" prefix is also used as BookmarkEntity.bookmarkId for chart pages, enabling the
//   bookmark and migration systems to identify chart bookmarks without an additional isChart flag.
//
// Fields:
//   id                — stable string identifier; matches the BookmarkEntity PK convention for charts.
//   chartTitle        — display title shown in the Charts list screen and search results.
//   subChapterTitle   — title of the sub-chapter that contains this chart; shown as subtitle in lists.
//   subChapterId      — foreign key into SubChapterEntity; used to join ChartAndSubChapter and to
//                       navigate to the correct HTML page when a chart is opened.
//   chartHomePosition — sort index controlling display order on the Charts home screen.
//
// Implements Parcelable via @Parcelize for navigation arguments. Seeded content.
// Related: SubChapterEntity, ChartAndSubChapter, ChartDao, GuideContentUpdater, BookmarkEntity.
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
    val subChapterId: Int,
    val chartHomePosition: Int
) : Parcelable
