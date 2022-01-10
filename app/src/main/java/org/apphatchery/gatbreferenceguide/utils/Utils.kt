package org.apphatchery.gatbreferenceguide.utils

import org.apphatchery.gatbreferenceguide.db.data.NoteColor

val CSS_JS_FILES = arrayOf("assets/uikit.css", "assets/uikit.js", "assets/uikit-icons.js")

val NOTE_COLOR = arrayListOf(
    NoteColor("#000000"),
    NoteColor("#FF2D55"),
    NoteColor("#AF52DE"),
    NoteColor("#FFCC00"),
    NoteColor("#34C759"),
    NoteColor("#5AC8FA"),
    NoteColor("#007AFF"),
    NoteColor("#5856D6"),
)

const val ANALYTICS_PAGE_EVENT = "page"
const val ANALYTICS_SEARCH_EVENT = "search"
const val ANALYTICS_BOOKMARK_EVENT = "bookmark"