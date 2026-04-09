// DAO for Contact — the TB programme staff directory embedded in the GA-TB Reference Guide.
// Contacts are seeded from a static list (ContactFragment.fakeContact) on every app launch:
// FAContactViewModel.init() calls clearContact() then insert() unconditionally. This means the
// contacts table is always a read-only mirror of the bundled data; user edits are not persisted
// here (the private-contact DAO path exists but is currently commented out in FAContactViewModel).
//
// getContacts(): ordered by fullName ASC and exposed as a Flow observed by FAContactViewModel,
// which converts it to LiveData for ContactFragment's FAContactAdapter. Adding a search-filter
// variant here (WHERE fullName LIKE ?) would be the natural extension if contact search is added.
//
// clearContact() is intentional "wipe before reseed" — it ensures stale contacts from a previous
// app version are removed before the fresh list is inserted with IGNORE conflict strategy.
//
// Related: Contact, FAContactViewModel, ContactFragment, FAContactAdapter.
package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.Contact

@Dao
interface ContactDao {

    /** Inserts the bundled staff directory. IGNORE strategy avoids errors if called twice;
     *  clearContact() is always called first in FAContactViewModel so duplicates never arise. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: List<Contact>)

    /** Live-observable contact list sorted A–Z, observed by ContactFragment via FAContactViewModel. */
    @Query("SELECT  * FROM  Contact ORDER BY fullName ASC")
    fun getContacts(): Flow<List<Contact>>

    @Delete
    suspend fun delete(data: Contact)

    /** Wipes all contacts before a reseed on every launch, guaranteeing the table reflects the
     *  current bundled list rather than stale data from a previous app version. */
    @Query("DELETE FROM Contact")
    suspend fun clearContact()
}