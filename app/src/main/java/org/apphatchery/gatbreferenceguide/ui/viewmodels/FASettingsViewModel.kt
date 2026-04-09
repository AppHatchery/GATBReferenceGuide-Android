// ViewModel backing SettingsFragment — manages the user-data reset flow.
//
// resetInfo(context): the single meaningful operation in this ViewModel. It performs a full
//   wipe of all user-generated data in the following order:
//     1. Clears all bookmarks (BookmarkDao.clearBookmarks).
//     2. Clears all notes (NoteDao.clearNotes).
//     3. Clears recent navigation history (RecentDao.clearRecent).
//     4. Removes the "RECENT_SEARCHES_LIST" key from the RECENT_SEARCHES SharedPreferences,
//        which holds the user's recent global search terms (stored as a serialised list,
//        separate from the Room recent-open history).
//   Guide content tables (chapters, subchapters, charts) are NOT touched — only user data.
//
// Related files: SettingsFragment, BookmarkDao, NoteDao, RecentDao, Database.
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