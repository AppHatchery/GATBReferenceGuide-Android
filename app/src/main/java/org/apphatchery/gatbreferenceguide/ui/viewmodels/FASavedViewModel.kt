package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity
import javax.inject.Inject

@HiltViewModel
class FASavedViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    val getBookmarkEntity = db.bookmarkDao().getBookmarkEntity().asLiveData()
    val getRecentEntity = db.recentDao().getRecentEntity().asLiveData()
    val getNoteEntity = db.noteDao().getNoteEntity().asLiveData()

    fun getChapterInfo(id: Int) = db.chapterDao().getChapterById(id).asLiveData()
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