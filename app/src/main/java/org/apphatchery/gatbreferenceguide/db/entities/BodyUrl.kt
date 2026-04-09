// Navigation payload passed between fragments when the user opens a guide content page.
// This is NOT a Room entity — it is a Parcelable bundle that travels inside a Fragment argument
// (Bundle) via the Navigation component, carrying everything the destination fragment needs to
// load and highlight the correct HTML page.
//
// Fields:
//   chapterEntity    — the parent chapter (e.g. "Chapter 4 – Treatment of TB Disease").
//   subChapterEntity — the specific sub-chapter whose HTML file should be loaded in the WebView.
//   searchQuery      — the keyword the user searched for; passed to the WebView so JavaScript
//                      can highlight matching text on the page. Empty string if no search active.
//
// Implements Parcelable via @Parcelize so it can be included in a NavDirections argument Bundle.
// Related: SubChapterEntity, ChapterEntity, FABodyFragment, GlobalSearchFragment, BodyUrl usage
//   in navigation graph arguments.
package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

@Parcelize
data class BodyUrl(
    val chapterEntity: ChapterEntity,
    val subChapterEntity: SubChapterEntity,
    val searchQuery: String
) : Parcelable
