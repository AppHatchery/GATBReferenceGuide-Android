// Room entity for the SubChapters table — represents one content page within a chapter of the
// GA-TB guide (e.g. "4.2 – Treatment Regimens for Drug-Susceptible TB").
//
// Table name: SubChapterEntity (Room default).
// Primary key: subChapterId — auto-generated integer. Although IDs are device-generated, the values
//   are stable within an install; they are used as FK references in NoteEntity, ChartEntity, and as
//   the basis for BookmarkEntity.bookmarkId (cast to String).
//
// Fields:
//   subChapterId    — PK; also stored in NoteEntity.subChapterId and ChartEntity.subChapterId.
//   chapterId       — FK into ChapterEntity; groups sub-chapters under their parent chapter for
//                     navigation and the ChapterAndSubChapter join query.
//   subChapterTitle — display title shown in the chapter detail list and search results.
//   url             — HTML asset filename (e.g. "chapter4_2.html") loaded by FABodyFragment in the
//                     WebView; matches HtmlInfoEntity.fileName.
//   lastUpdated     — version/date string from the source guide data; not currently displayed in UI
//                     but retained for future change-tracking or "Updated" badges.
//
// Implements Parcelable via @Parcelize for navigation arguments (BodyUrl). Seeded content.
// Related: ChapterEntity, ChapterAndSubChapter, SubChapterDao, HtmlInfoEntity, NoteEntity, BodyUrl.
package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class SubChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val subChapterId: Int = 0,
    val chapterId: Int,
    val subChapterTitle: String,
    val url: String ="",
    val lastUpdated: String =""
) : Parcelable
