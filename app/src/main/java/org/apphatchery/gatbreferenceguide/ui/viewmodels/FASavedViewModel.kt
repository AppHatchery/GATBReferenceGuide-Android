// ViewModel backing SavedFragment — shows the user's bookmarks, notes, and recent history.
// SavedFragment has three tabs; this ViewModel serves all three and uses savedItemCount to
// track per-tab badge counts for the tab bar UI.
//
// Reactive LiveData:
//   getBookmarkEntity  – all BookmarkEntity rows, ordered for display in the Bookmarks tab.
//   getRecentEntity    – all RecentEntity rows, ordered by insertion (newest first) for Recents.
//   getNoteEntity      – all NoteEntity rows for the Notes tab.
// savedItemCount (StateFlow<SavedTypeData>): holds the per-tab count summary; updated by
//   setSavedItemCount() when the fragment knows how many items are in each tab.
//
// Bookmark repair: repairRedirectedBookmark() updates a stored bookmark whose URL was renamed
//   in a content update, preserving the user's bookmark without requiring them to re-bookmark.
// OrNull variants (getChapterInfoOrNull, getSubChapterInfoOrNull, etc.) return null instead of
//   throwing when an ID no longer exists — used during bookmark repair validation.
//
// Related files: SavedFragment, BookmarkDao, NoteDao, RecentDao, BookmarkEntity, Database.
package org.apphatchery.gatbreferenceguide.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity
import org.apphatchery.gatbreferenceguide.ui.fragments.SavedFragment
import javax.inject.Inject

@HiltViewModel
class FASavedViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    val getBookmarkEntity = db.bookmarkDao().getBookmarkEntity().asLiveData()
    val getRecentEntity = db.recentDao().getRecentEntity().asLiveData()
    val getNoteEntity = db.noteDao().getNoteEntity().asLiveData()

    private val _savedItemCount = MutableStateFlow(SavedFragment.SavedTypeData())

    val savedItemCount: StateFlow<SavedFragment.SavedTypeData> = _savedItemCount

    fun setSavedItemCount(savedTypeData: SavedFragment.SavedTypeData) {
        _savedItemCount.value = savedTypeData
    }

    fun getChapterInfo(id: Int) = db.chapterDao().getChapterById(id).asLiveData()
    fun getChapterInfoOrNull(id: Int) = db.chapterDao().getChapterByIdOrNull(id).asLiveData()
    fun getChartAndSubChapterById(id: String) = db.chartDao().getChartAndSubChapterById(id).asLiveData()
    fun getChartAndSubChapterByIdOrNull(id: String) = db.chartDao().getChartAndSubChapterByIdOrNull(id).asLiveData()
    fun getSubChapterInfo(id: String) = db.subChapterDao().getSubChapterById(id).asLiveData()
    fun getSubChapterInfoOrNull(id: String) = db.subChapterDao().getSubChapterByIdOrNull(id).asLiveData()

    fun deleteBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().delete(data)
    }


    fun insertBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().insert(data)
    }

    fun updateBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().update(data)
    }

    /**
     * Repairs a bookmark whose content URL was renamed in a guide content update.
     * Updates the stored [oldId] to [newId] along with the new title and subchapter label,
     * so the bookmark continues to navigate correctly without user intervention.
     * Delegates to BookmarkDao.repairRedirect() which runs a targeted UPDATE query.
     */
    fun repairRedirectedBookmark(
        oldId: String,
        newId: String,
        newTitle: String,
        newSubChapter: String,
    ) = viewModelScope.launch {
        db.bookmarkDao().repairRedirect(
            oldId = oldId,
            newId = newId,
            newTitle = newTitle,
            newSubChapter = newSubChapter,
        )
    }


    fun deleteNote(data: NoteEntity) = viewModelScope.launch {
        db.noteDao().delete(data)
    }


    fun insertNote(data: NoteEntity) = viewModelScope.launch {
        db.noteDao().insert(data)
    }

}