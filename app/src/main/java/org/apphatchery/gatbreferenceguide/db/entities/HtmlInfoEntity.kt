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
