package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class PrivateContact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val additionalInfo: String = "",
    val contactCell: String = "",
    val contactEmail: String = "",
    val contactAddress: String = "",
    val officePhone: String = "",
    val officeFax: String = "",
    val personalNote: String? = null
) : Parcelable
