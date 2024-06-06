package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity
import org.apphatchery.gatbreferenceguide.ui.fragments.BookmarksFragment
import org.apphatchery.gatbreferenceguide.ui.fragments.SavedFragment

import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {
    fun setSavedItemCount(savedTypeData: SavedFragment.SavedTypeData) {

    }

    // TODO: Implement the ViewModel
    val getBookmarkEntity = db.bookmarkDao().getBookmarkEntity().asLiveData()

    private val _savedItemCount = MutableStateFlow(BookmarksFragment.SavedTypeData())

    val savedItemCount: StateFlow<BookmarksFragment.SavedTypeData> = _savedItemCount

    fun getChapterInfo(id: Int) = db.chapterDao().getChapterById(id).asLiveData()
    fun getChartAndSubChapterById(id: String) =
        db.chartDao().getChartAndSubChapterById(id).asLiveData()

    fun getSubChapterInfo(id: String) = db.subChapterDao().getSubChapterById(id).asLiveData()

    fun deleteBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().delete(data)
    }


    fun insertBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().insert(data)
    }


    fun deleteNote(data: NoteEntity) = viewModelScope.launch {
        db.noteDao().delete(data)
    }


    fun insertNote(data: NoteEntity) = viewModelScope.launch {
        db.noteDao().insert(data)
    }
}