package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.entities.*
import org.apphatchery.gatbreferenceguide.db.repositories.Repository
import javax.inject.Inject

@HiltViewModel
class FAMainViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {

    val getChapter = repo.db.chapterDao().getChapterEntity().asLiveData()
    val getChart = repo.db.chartDao().getChartAndSubChapter().asLiveData()
    fun getChapterInfo(id: Int) = repo.db.chapterDao().getChapterById(id).asLiveData()
    fun getSubChapterInfo(id: String) = repo.db.subChapterDao().getSubChapterById(id).asLiveData()
    fun getChartAndSubChapterById(id: String) =
        repo.db.chartDao().getChartAndSubChapterById(id).asLiveData()


    var dumpChartDataObserve = true
    var dumpSubChapterDataObserver = true
    private val taskFlowChannel = Channel<Callback>()
    val taskFlowEvent = taskFlowChannel.receiveAsFlow()

    fun purgeData(){
        repo.purgeData()
    }


    fun dumpChapterData(data: List<ChapterEntity>) = repo.dumpChapterInfo(data).asLiveData()

    fun dumpChartData(data: List<ChartEntity>) = repo.dumpChartInfo(data).asLiveData()

    fun dumpSubChapterData(data: List<SubChapterEntity>) =
        repo.dumpSubChapterInfo(data).asLiveData()


    fun dumpHTMLInfo(data: ArrayList<HtmlInfoEntity>) = viewModelScope.launch {
        repo.db.htmlInfoDao().insert(data)
        taskFlowChannel.send(Callback.InsertHTMLInfoComplete)
    }


    fun bindHtmlWithChapter() = viewModelScope.launch {

        val globalSearch = ArrayList<GlobalSearchEntity>()


        repo.db.subChapterDao().getSubChapterBindChapterSuspended().forEach { data ->
            data.subChapterEntity.forEach {
                globalSearch.add(
                    GlobalSearchEntity(
                        data.chapterEntity.chapterTitle,
                        it.subChapterTitle,
                        javaClass.name,
                        it.url,
                        it.chapterId,
                        it.subChapterId
                    )
                )
            }
        }

        repo.db.chartDao().getChartAndSubChapterSuspend().forEach {
            globalSearch.add(
                GlobalSearchEntity(
                    it.chartEntity.chartTitle,
                    it.subChapterEntity.subChapterTitle,
                    javaClass.name,
                    it.chartEntity.id,
                    it.subChapterEntity.chapterId,
                    it.subChapterEntity.subChapterId,
                    true
                )
            )
        }


        val globalSearchComplete = ArrayList<GlobalSearchEntity>()
        repo.db.htmlInfoDao().getHtmlInfoEntitySuspended().forEach { htmlInfoEntity ->
            globalSearch.forEach { globalSearch ->
                if (globalSearch.fileName == htmlInfoEntity.fileName) {
                    globalSearchComplete.add(
                        GlobalSearchEntity(
                            globalSearch.searchTitle,
                            globalSearch.subChapter,
                            htmlInfoEntity.htmlText,
                            globalSearch.fileName,
                            globalSearch.chapterId,
                            globalSearch.subChapterId,
                            globalSearch.isChart
                        )
                    )
                }
            }
        }

        repo.db.withTransaction {
            repo.db.globalSearchDao().insert(globalSearchComplete)
            taskFlowChannel.send(Callback.InsertGlobalSearchInfoComplete)
        }

    }


    sealed class Callback {
        object InsertHTMLInfoComplete : Callback()
        object InsertGlobalSearchInfoComplete : Callback()
    }
}