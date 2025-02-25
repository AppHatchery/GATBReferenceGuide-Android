package org.apphatchery.gatbreferenceguide.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
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


    fun resetInfo(context: Context) = viewModelScope.launch  {
        db.bookmarkDao().clearBookmarks()
        db.noteDao().clearNotes()
        db.recentDao().clearRecent()
        val sharedPreferences = context.getSharedPreferences("RECENT_SEARCHES", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("RECENT_SEARCHES_LIST").apply()
    }


}