package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.repositories.DownloadFileRepo
import javax.inject.Inject

@HiltViewModel
class FAMainViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {

    val getChapter = db.chapterDao().getChapterEntity().asLiveData()
    val getChart = db.chartDao().getChartAndSubChapter().asLiveData()
    fun getChapterInfo(id: Int) = db.chapterDao().getChapterById(id).asLiveData()
    fun getSubChapterInfo(id: String) = db.subChapterDao().getSubChapterById(id).asLiveData()
    fun getChartAndSubChapterById(id: String) = db.chartDao().getChartAndSubChapterById(id).asLiveData()

    fun getCountByChapterId(chapterId: Int) = db.subChapterDao().getCountByChapterId(chapterId).asLiveData()
}