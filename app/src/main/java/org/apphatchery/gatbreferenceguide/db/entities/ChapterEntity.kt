// Room entity for the Chapters table — represents one top-level chapter of the GA-TB guide
// (e.g. "Chapter 1 – Epidemiology of TB in Georgia").
//
// Table name: ChapterEntity (Room default).
// Primary key: chapterId — a stable integer assigned at seeding time (autoGenerate = false).
//   This ID is referenced as a foreign key in SubChapterEntity.chapterId and stored in
//   GlobalSearchEntity.chapterId to enable hierarchical navigation.
//
// Fields:
//   chapterId           — numeric chapter identifier; must match the guide source data.
//   chapterTitle        — display title shown in the main chapter list and navigation drawer.
//   chapterHomePosition — scroll offset used to restore the user's last position on the Home screen
//                         chapter list; 0 means top of list.
//
// Implements Parcelable via @Parcelize so instances can be included in navigation arguments (BodyUrl).
// Seeded content — wiped and re-seeded on every guide update via GuideContentUpdater.kt.
// Related: SubChapterEntity, ChapterAndSubChapter, ChapterDao, GuideContentUpdater, BodyUrl.
package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class ChapterEntity(
    @PrimaryKey(autoGenerate = false)
    val chapterId: Int = 0,
    val chapterTitle: String,
    val chapterHomePosition:Int = 0,
) : Parcelable
