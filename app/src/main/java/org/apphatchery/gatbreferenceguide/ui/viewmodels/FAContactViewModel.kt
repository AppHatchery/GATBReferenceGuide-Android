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