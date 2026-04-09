// Room entity for the Contact table — stores publicly visible TB-program contacts managed by the
// app (e.g. clinic coordinators, regional TB officers) that are pre-loaded or administrator-added.
//
// Table name: Contact (Room default).
// Primary key: id — auto-generated integer; the app treats contacts as mutable records rather than
//   content-addressed items, so auto-increment is appropriate here.
//
// Fields:
//   fullName        — contact's display name, the only required field.
//   additionalInfo  — job title, organisation, or other freeform descriptor (optional).
//   contactCell     — mobile phone number shown on the contact detail screen.
//   contactEmail    — email address; shown as a tappable mailto: link in the UI.
//   contactAddress  — physical office or mailing address (optional).
//   officePhone     — direct-dial office number (optional).
//   officeFax       — fax number, shown when populated (optional).
//   personalNote    — nullable private memo visible only to the device user.
//
// Implements Parcelable via @Parcelize for navigation arguments. User data — never auto-wiped.
// See also PrivateContact for the user's own private contacts. Related: ContactDao, ContactFragment.
package org.apphatchery.gatbreferenceguide.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Contact(
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
