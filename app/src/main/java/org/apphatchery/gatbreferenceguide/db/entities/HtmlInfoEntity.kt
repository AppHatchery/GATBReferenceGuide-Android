// Room entity for the HtmlInfo table — caches the raw HTML text of every guide page so the app
// can function fully offline and seed the FTS index without reading asset files at runtime.
//
// Table name: HtmlInfoEntity (Room default).
// Primary key: fileName — the HTML asset filename (e.g. "chapter4_2.html"); autoGenerate = false
//   because the filename is a natural, stable key matching the asset bundled in the APK.
//   SubChapterEntity.url also stores this filename, so the two tables join logically on fileName.
//
// Fields:
//   fileName — the asset filename; used by FABodyFragment to load the correct page in the WebView
//              and by GuideContentUpdater to populate GlobalSearchEntity.textInBody during seeding.
//   htmlText — the complete HTML string for the page, used during FTS seeding. After seeding,
//              the WebView loads directly from assets rather than this field to avoid large reads.
//
// Implements Parcelable via @Parcelize. Seeded content — wiped and re-seeded on guide updates.
// Related: SubChapterEntity, HtmlInfoDao, GlobalSearchEntity, GuideContentUpdater, FABodyFragment.
package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class HtmlInfoEntity(
    @PrimaryKey(autoGenerate = false)
    val fileName: String,
    val htmlText: String
) : Parcelable
