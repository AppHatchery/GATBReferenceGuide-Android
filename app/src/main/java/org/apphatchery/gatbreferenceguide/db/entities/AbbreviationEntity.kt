package org.apphatchery.gatbreferenceguide.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class AbbreviationEntity(
    @PrimaryKey(autoGenerate = false)
    val abbrId: Int,
    val abbreviation: String,
    val meaning: String
)