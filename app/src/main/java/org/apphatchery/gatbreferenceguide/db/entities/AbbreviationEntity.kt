// Room entity for the Abbreviations table — stores the medical shorthand used throughout the
// GA-TB Reference Guide (e.g. "MDR-TB" → "Multi-Drug Resistant Tuberculosis").
//
// Table name: AbbreviationEntity (Room default from class name).
// Primary key: abbrId — a stable integer assigned at content-seeding time; autoGenerate = false
//   because IDs originate from the guide's source data, not the device.
//
// Fields:
//   abbrId       — numeric identifier, unique per abbreviation entry.
//   abbreviation — the short form shown in the glossary list (e.g. "DOTS").
//   meaning      — the full expansion displayed when the user taps an entry.
//
// This table is seeded content: wiped and re-seeded on every guide update via GuideContentUpdater.
// Related: GuideContentUpdater.kt, Database.kt.
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
