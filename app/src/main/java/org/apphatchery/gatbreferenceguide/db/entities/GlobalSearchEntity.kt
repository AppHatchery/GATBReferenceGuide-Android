// Room FTS4 virtual table backing the app-wide full-text search across all guide content.
// @Fts4 tells Room to create an SQLite FTS4 virtual table instead of a regular B-tree table;
// this makes MATCH queries on thousands of HTML body texts orders of magnitude faster than LIKE.
// Note: FTS4 tables have no explicit primary key — Room manages the hidden rowid internally.
//
// Table name: GlobalSearchEntity (Room default FTS virtual table).
//
// Fields:
//   searchTitle  — page/section title indexed for search hit display.
//   subChapter   — sub-chapter title, shown as a subtitle in search result rows.
//   textInBody   — full stripped text of the HTML page; the primary FTS search target.
//   fileName     — HTML asset filename used to load the page when a result is tapped.
//   chapterId    — chapter FK, used to ORDER BY in GlobalSearchDao for grouped results.
//   subChapterId — sub-chapter FK, used alongside chapterId for result ordering.
//   isChart      — true when this entry describes a chart/table page vs. a narrative page.
//   chartId      — ChartEntity.id for chart pages; empty string for regular sub-chapter pages.
//
// Seeded content — dropped and rebuilt on every guide update. Related: GlobalSearchDao,
// GlobalSearchWithMatchInfo, FAGlobalSearchViewModel, GlobalSearchFragment, GuideContentUpdater.
package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.Fts3
import androidx.room.Fts4

@Entity
@Fts4
data class GlobalSearchEntity(
    val searchTitle: String,
    val subChapter: String,
    val textInBody: String,
    val fileName: String = "",
    val chapterId: Int = 0,
    val subChapterId: Int = 0,
    val isChart: Boolean = false,
    val chartId: String = ""
)


