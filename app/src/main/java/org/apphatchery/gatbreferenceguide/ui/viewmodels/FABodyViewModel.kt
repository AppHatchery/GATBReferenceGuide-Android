// ViewModel backing BodyFragment — the main content viewer that renders a guide chapter/section.
// Provides LiveData-wrapped access to bookmarks, notes, subchapters, and recent history so
// BodyFragment observes changes reactively without holding a direct database reference.
// Receives the Database singleton via Hilt @Inject constructor injection.
//
// Bookmark functions: getBookmarkById(id) lets BodyFragment observe whether the current page is
// saved, driving the bookmark toggle icon state. insert/delete/update operate on BookmarkEntity.
//
// Note functions: each guide page can have one personal note keyed by subChapter ID. getNote(id)
// returns a Flow observed to show/hide the note editor. insert/update/delete manage its lifecycle.
//
// recentOpen(data): deliberately does DELETE then INSERT rather than upsert. Room's REPLACE
// strategy updates the existing row in-place, keeping its original rowid and sort position.
// Delete + insert creates a new row with a new rowid, moving the entry to the TOP of the
// recent list (which is ordered by insertion rowid). This is intentional "bump to top" behavior.
//
// getSubChapter: exposed as a LiveData list consumed by BodyFragment's subchapter navigation drawer.
// Related: BodyFragment, BookmarkEntity, NoteEntity, RecentEntity, Database, FASavedViewModel.
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
class FABodyViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    fun getChapterById(id: Int) = db.chapterDao().getChapterById(id).asLiveData()

    fun getBookmarkById(id: String) =
        db.bookmarkDao().getBookmarkById(id).asLiveData()

    val getSubChapter = db.subChapterDao().getSubChapter().asLiveData()

    fun insertBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().insert(data)
    }

    fun deleteBookmark(data: BookmarkEntity) = viewModelScope.launch {
        db.bookmarkDao().delete(data)
    }

    fun getNote(id: String) =
        db.noteDao().getNoteById(id).asLiveData()

    fun insertNote(noteEntity: NoteEntity) = viewModelScope.launch {
        db.noteDao().insert(noteEntity)
    }

    fun updateNote(data: NoteEntity) = viewModelScope.launch  {
        db.noteDao().update(data)
    }

    fun deleteNote(note: NoteEntity) = viewModelScope.launch {
        db.noteDao().delete(note)
    }

    fun recentOpen(data: RecentEntity) = viewModelScope.launch {
        // Delete first, then re-insert to give this entry a new rowid, which moves it to the
        // top of the recent list. A plain upsert (REPLACE) would keep the old rowid and position.
        db.recentDao().delete(data)
        db.recentDao().insert(data)
    }

    fun updateBookmark(data: BookmarkEntity) = viewModelScope.launch  {
        db.bookmarkDao().update(data)
    }
}
