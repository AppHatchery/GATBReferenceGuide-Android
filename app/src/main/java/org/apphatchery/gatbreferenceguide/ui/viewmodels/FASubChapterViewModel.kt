package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity
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

    fun recentOpen(data: RecentEntity) = viewModelScope.launch {
        db.recentDao().delete(data)
        db.recentDao().insert(data)
    }
}