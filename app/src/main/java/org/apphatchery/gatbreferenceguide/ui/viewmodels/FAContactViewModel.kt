// ViewModel backing ContactFragment — shows the TB programme's public contact directory.
// Contacts are seeded from ContactFragment.fakeContact (a hardcoded list) and replaced on
// every ViewModel init, so the list always reflects the bundled data rather than stale DB rows.
//
// getContacts (LiveData<List<Contact>>): the full public contact list, observed by ContactFragment.
// contactItemCount (StateFlow<ContactTypeData>): tracks tab/badge counts (e.g. public vs saved
//   contacts) for the ContactFragment tab UI; updated by setSavedItemCount().
//
// insert(): clears the contacts table and re-inserts fakeContact on every init — this is
//   intentional "refresh from bundled data" behavior, not a bug. Runs on viewModelScope.
// deleteContact / update / copyPublicToPrivateContact: private-contact operations are currently
//   commented out (PrivateContact feature not yet shipped).
//
// Related files: ContactFragment, SavedFragment, ContactDao, Contact, Database.
package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.db.entities.PrivateContact
import org.apphatchery.gatbreferenceguide.ui.fragments.ContactFragment
import org.apphatchery.gatbreferenceguide.ui.fragments.ContactFragment.Companion.fakeContact
import org.apphatchery.gatbreferenceguide.ui.fragments.SavedFragment
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FAContactViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    private val _contactItemCount = MutableStateFlow(ContactFragment.ContactTypeData())

    val contactItemCount: StateFlow<ContactFragment.ContactTypeData> = _contactItemCount

    fun setSavedItemCount(contactTypeData: ContactFragment.ContactTypeData) {
        _contactItemCount.value = contactTypeData
    }


    init {
        insert()
    }

    fun insert() = viewModelScope.launch {
        db.contactDao().clearContact()
        db.contactDao().insert(fakeContact)
    }

    fun deleteContact(data:PrivateContact) = viewModelScope.launch{
//        db.privateContactDao().delete(data)
    }

    fun update(data:PrivateContact) = viewModelScope.launch{
//        db.privateContactDao().update(data)
    }

    fun copyPublicToPrivateContact(data:PrivateContact) = viewModelScope.launch{
//        db.privateContactDao().insert(data)
    }

//    val getPrivateContact = db.privateContactDao().getContacts().asLiveData()

    val getContacts = db.contactDao().getContacts().asLiveData()
}