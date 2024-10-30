package org.apphatchery.gatbreferenceguide.retrofit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_pages")
data class WebPage(
    @PrimaryKey val id: String,
    val content: String
)
