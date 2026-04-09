// Distinguishes the two kinds of item a user can bookmark in the TB guide.
//
//   SUBCHAPTER — a guide article page (an HTML page under pages/); its ID is the numeric
//                subChapterId from SubChapterEntity. Tapping navigates to BodyFragment.
//   CHART      — a reference table (a chart page); its ID is the chartId string from
//                ChartEntity (e.g. "table_first_line_tb_drugs"). Tapping navigates to the
//                chart detail view.
//
// Stored in BookmarkEntity.bookmarkType (as the enum name string via Room TypeConverter).
// Used in FASavedViewModel and SavedFragment to decide which navigation action to take
// when a bookmark row is tapped, and which DAO method to call for deletions.
// Related: BookmarkEntity.kt, BookmarkDao.kt, FASavedViewModel, SavedFragment.
package org.apphatchery.gatbreferenceguide.enums

enum class BookmarkType {
    SUBCHAPTER, CHART
}