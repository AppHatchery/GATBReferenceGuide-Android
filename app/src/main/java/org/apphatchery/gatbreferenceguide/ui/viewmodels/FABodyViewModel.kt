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
class FABodyViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    fun getChapterById(id: Int) = db.chapterDao().getChapterById(id).asLiveData()

    fun getBookmarkById(id: Int, fetchByChapter: Boolean = false) =
        db.bookmarkDao().getBookmarkById(id, fetchByChapter).asLiveData()

    val getSubChapter = db.subChapterDao().getSubChapter().asLiveData()

    fun insertBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().insert(data)
    }

    fun deleteBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().delete(data)
    }

    fun getNote(isSubChapter: Boolean, subOrChapterId: Int) =
        db.noteDao().getNoteEntity(isSubChapter, subOrChapterId).asLiveData()

    fun insertNote(noteEntity: NoteEntity) = viewModelScope.launch {
        db.noteDao().insert(noteEntity)
    }


    fun deleteNote(note: NoteEntity) = viewModelScope.launch {
        db.noteDao().delete(note)
    }
}