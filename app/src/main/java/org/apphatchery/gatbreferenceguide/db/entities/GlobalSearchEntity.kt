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

