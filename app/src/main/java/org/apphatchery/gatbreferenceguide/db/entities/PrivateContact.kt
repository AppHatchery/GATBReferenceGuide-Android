// Room entity for the PrivateContact table — stores personal contacts added by the device user,
// separate from the administrator-managed Contact table.
//
// Table name: PrivateContact (Room default).
// Primary key: id — auto-generated integer; contacts are mutable user records, not content-addressed.
//
// The schema is intentionally identical to Contact so the UI can render both lists with a shared
// adapter/layout. The separation into two tables allows different access controls and ensures that
// admin-seeded contacts (Contact) cannot be accidentally mutated through the private contacts UI.
//
// Fields:
//   fullName       — contact's display name; the only required field.
//   additionalInfo — job title, organisation, or other freeform descriptor (optional).
//   contactCell    — mobile phone number (optional).
//   contactEmail   — email address shown as a tappable mailto: link (optional).
//   contactAddress — physical office or mailing address (optional).
//   officePhone    — direct-dial office number (optional).
//   officeFax      — fax number (optional).
//   personalNote   — nullable private memo visible only to this device user.
//
// User data — never auto-wiped. Related: Contact, ContactDao, PrivateContactFragment.
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
