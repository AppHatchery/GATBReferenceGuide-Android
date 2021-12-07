package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.ui.fragments.ContactFragment.Companion.fakeContact
import javax.inject.Inject

@HiltViewModel
class FAContactViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    init {
        insert()
    }

    private fun insert() = viewModelScope.launch {
        db.contactDao().clearContact()
        db.contactDao().insert(fakeContact)
    }


    val getContacts = db.contactDao().getContacts().asLiveData()
}