package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.Fts4

@Entity
@Fts4(contentEntity = GlobalSearchEntity::class)
data class FullTextSearchGlobalSearchEntity(
    val searchTitle: String,
    val subChapter: String,
    val textInBody: String,
    val fileName: String
)

