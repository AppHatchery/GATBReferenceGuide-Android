package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.apphatchery.gatbreferenceguide.db.Database
import javax.inject.Inject

@HiltViewModel
class FASubChapterViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {


    var chapterId = 0
    val searchQuery = MutableStateFlow("")

    private val taskFlow = searchQuery.flatMapLatest {
        db.subChapterDao().getSubChapterEntity(it, chapterId, true)
    }

    val getSubChapterEntity = taskFlow.asLiveData()
}