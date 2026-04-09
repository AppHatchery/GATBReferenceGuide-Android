// App-wide singletons and constants that coordinate UI state across fragments.
//
// CSS_JS_FILES: the three UIkit files that must be injected into every guide page WebView.
//               Order matters — uikit.css before uikit.js before uikit-icons.js.
//
// NOTE_COLOR: the fixed palette of 9 note annotation colours shown in the note editor.
//             Corresponds to the colour picker swatches in the notes bottom sheet.
//
// Analytics event name constants (ANALYTICS_PAGE_EVENT, ANALYTICS_SEARCH_EVENT,
// ANALYTICS_BOOKMARK_EVENT): passed to Pendo.track() calls; keep in sync with the
// Pendo dashboard event names.
//
// searchState: process-wide flag that marks whether the user is currently in global-search
//              mode. BodyFragment reads this to decide whether to highlight search terms
//              after loading a page. NOT thread-safe — only touch from the main thread.
//
// HighlightedWordSingleton: holds the list of GlobalSearchEntity words to highlight in the
//              WebView after a global-search navigation. Set by GlobalSearchFragment before
//              navigating; read by BodyFragment on page load.
// Related: GlobalSearchFragment, BodyFragment, NoteColor.kt, GlobalSearchEntity.kt.
package org.apphatchery.gatbreferenceguide.utils

import org.apphatchery.gatbreferenceguide.db.data.NoteColor
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity

val CSS_JS_FILES = arrayOf("assets/uikit.css", "assets/uikit.js", "assets/uikit-icons.js")

val NOTE_COLOR = arrayListOf(
    NoteColor("#000000"),
    NoteColor("#FF2D55"),
    NoteColor("#ff9500"),
    NoteColor("#FFCC00"),
    NoteColor("#34C759"),
    NoteColor("#5AC8FA"),
    NoteColor("#007AFF"),
    NoteColor("#5856D6"),
    NoteColor("#af52de")
)

const val ANALYTICS_PAGE_EVENT = "page"
const val ANALYTICS_SEARCH_EVENT = "search"

const val ANALYTICS_BOOKMARK_EVENT = "bookmark"

/**
 * Process-wide toggle for global-search mode.
 * Call [enterSearchMode] before navigating from GlobalSearchFragment to BodyFragment so the
 * body knows to run the highlight JS injection. Call [exitSearchMode] in MainFragment.init()
 * to reset the flag when the user returns to the home screen.
 * Not thread-safe; always access from the main thread.
 */
object searchState {
    enum class SearchState {
        IN_SEARCH,
        OUT_OF_SEARCH
    }
     var currentState = SearchState.OUT_OF_SEARCH
    fun enterSearchMode() {
        currentState = SearchState.IN_SEARCH
    }

    // Function to change the state to OUT_OF_SEARCH
    fun exitSearchMode() {
        currentState = SearchState.OUT_OF_SEARCH
    }

}


/**
 * Transient store for the search terms that should be highlighted after a global-search
 * navigation. GlobalSearchFragment sets this before navigating; BodyFragment reads it once
 * during page-load to inject the highlight JavaScript. Cleared implicitly when a new search
 * is performed or when exitSearchMode() is called.
 */
object HighlightedWordSingleton {
    private var highlightedWordInstance: List<GlobalSearchEntity>? = null

    fun setHighlightedWord(wordList: List<GlobalSearchEntity>) {
        highlightedWordInstance = wordList
    }

    fun getHighlightedWord(): List<GlobalSearchEntity>? {
        return highlightedWordInstance
    }
}

