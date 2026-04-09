// ViewModel backing SubChapterFragment — displays the list of sections within a single chapter.
// The user arrives here after tapping a chapter in ChapterFragment.
//
// chapterId: set by SubChapterFragment before observing; controls which chapter's sections load.
// searchQuery (MutableStateFlow<String>): drives live search filtering within the subchapter list.
// getSubChapterEntity (LiveData<List<SubChapterEntity>>): filtered subchapter list, re-emitted on
//   each searchQuery change via flatMapLatest. Subchapter ID 28 is filtered out in this ViewModel
//   (its content page was removed from the guide but kept in the DB for referential integrity).
//
// recentOpen(data): records a subchapter as recently visited using DELETE then INSERT rather than
//   upsert. This "bump to top" pattern ensures the entry sorts to the top of the recent list
//   (ordered by rowid/insertion order). See also FABodyViewModel for the same pattern.
//
// Related files: SubChapterFragment, Database, SubChapterDao, RecentDao, RecentEntity.
package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity
import javax.inject.Inject

@HiltViewModel
class FASubChapterViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    // The page for subchapterId 28 was removed from content. kept it in DB for
    // referential integrity, but hide it from the visible subchapter list to avoid
    // broken navigation from the list screen.
    private val hiddenSubChapterIds = setOf(28)

    var chapterId = 0
    val searchQuery = MutableStateFlow("")

    private val taskFlow = searchQuery.flatMapLatest {
        db.subChapterDao().getSubChapterEntity(it, chapterId, true)
    }

    // Filter out hidden subchapters from the list provided to the UI layer.
    val getSubChapterEntity = taskFlow
        .map { list -> list.filterNot { it.subChapterId in hiddenSubChapterIds } }
        .asLiveData()

    fun getChapterInfo(chapterId: Int) = db.chapterDao().getChapterById(chapterId).asLiveData()

    fun recentOpen(data: RecentEntity) = viewModelScope.launch {
        db.recentDao().delete(data)
        db.recentDao().insert(data)
    }
}