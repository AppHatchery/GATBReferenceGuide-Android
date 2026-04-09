// Room entity for the Recents table — records guide pages the user has recently visited,
// powering the "Recently Viewed" list on the Home screen.
//
// Table name: RecentEntity (Room default).
// Primary key: id — a String content identifier (mirrors BookmarkEntity.bookmarkId convention:
//   numeric string for sub-chapter pages, "table_*" for chart pages); autoGenerate = false so
//   re-visiting the same page updates the existing row's timestamp rather than creating a duplicate.
//
// Fields:
//   id        — stable content ID matching SubChapterEntity or ChartEntity; used as PK to enforce
//               at-most-one entry per page. Insert strategy in RecentDao uses REPLACE on conflict.
//   title     — display title of the visited page, shown in the recents list.
//   timeStamp — epoch-millisecond time of last visit; RecentDao orders by DESC so the most recent
//               page appears first. Defaults to the time of insertion.
//
// User data — never auto-wiped. The recents list is ordered by timeStamp DESC in RecentDao.
// Related: RecentDao, RecentSearchItem, FAHomeViewModel, HomeFragment.
package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val timeStamp: Long = System.currentTimeMillis()
)
