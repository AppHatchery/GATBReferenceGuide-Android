package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.apphatchery.gatbreferenceguide.db.Database
import javax.inject.Inject

@HiltViewModel
class FAChartViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {

    val getChart = db.chartDao().getChartAndSubChapter().asLiveData()
    fun getChapterInfo(id: Int) = db.chapterDao().getChapterById(id).asLiveData()

}