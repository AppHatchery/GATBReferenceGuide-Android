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