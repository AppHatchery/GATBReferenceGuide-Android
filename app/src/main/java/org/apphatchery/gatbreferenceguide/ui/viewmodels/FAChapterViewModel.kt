// ViewModel backing ChapterFragment — the top-level chapter list screen of the TB guide.
// Exposes a searchable list of chapters so the user can navigate to a specific guide chapter.
//
// getChapterEntity (LiveData<List<ChapterEntity>>): reactive chapter list, re-emitted whenever
//   searchQuery changes. Uses flatMapLatest so rapid typing cancels in-flight DB queries.
// searchQuery (MutableStateFlow<String>): updated by ChapterFragment's search bar; empty string
//   returns all chapters.
//
// getCountByChapterId(chapterId): returns the subchapter count for a chapter, used to show
//   item counts in the chapter list UI (e.g., "5 topics").
//
// Related files: ChapterFragment, Database, ChapterDao, SubChapterDao.
package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.apphatchery.gatbreferenceguide.db.Database
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class FAChapterViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    private val searchQuery = MutableStateFlow("")

    private val taskFlow = searchQuery.flatMapLatest {
        db.chapterDao().getChapterEntity(it)
    }

    val getChapterEntity = taskFlow.asLiveData()

    fun getCountByChapterId(chapterId: Int) =
        db.subChapterDao().getCountByChapterId(chapterId).asLiveData()

//    fun getSubChapterById(id: Int) = db.subChapterDao().getSubChapterById(id).asLiveData()


}