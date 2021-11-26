package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity
import javax.inject.Inject

@HiltViewModel
class FASettingsViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {



    fun resetInfo() = viewModelScope.launch  {
        db.bookmarkDao().clearBookmarks()
        db.noteDao().clearNotes()
        db.recentDao().clearRecent()
    }


}