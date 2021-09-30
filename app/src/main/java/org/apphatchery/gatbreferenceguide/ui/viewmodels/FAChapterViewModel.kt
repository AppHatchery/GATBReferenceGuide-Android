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