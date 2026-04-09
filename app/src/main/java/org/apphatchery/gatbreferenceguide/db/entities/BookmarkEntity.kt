// Room entity for the Bookmarks table — stores guide pages saved by the user for quick re-access.
//
// Table name: BookmarkEntity (Room default).
// Primary key: bookmarkId — a String, NOT auto-generated. For regular sub-chapter pages it holds
//   the SubChapterEntity.subChapterId cast to String; for chart/table pages it holds a "table_*"
//   prefixed string. This stable, meaningful ID allows BookmarkDao.repairRedirect() to update
//   bookmarks in-place when guide content is renumbered across app versions.
//
// Fields:
//   bookmarkId    — stable content ID; doubles as PK to prevent duplicate bookmarks for the same page.
//   bookmarkTitle — human-readable page title shown in the Saved screen list.
//   subChapter    — the sub-chapter title string used for display grouping in the Saved screen.
//
// User data — never auto-wiped during guide content updates (unlike seeded tables).
// Related: BookmarkDao.kt, FASavedViewModel, SavedFragment, LegacyRedirects.kt, LegacyNotesMigrator.kt.
package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = false)
    val bookmarkId: String = 0.toString(),
    val bookmarkTitle: String = "",
    val subChapter: String = ""
)
