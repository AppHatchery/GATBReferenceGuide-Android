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