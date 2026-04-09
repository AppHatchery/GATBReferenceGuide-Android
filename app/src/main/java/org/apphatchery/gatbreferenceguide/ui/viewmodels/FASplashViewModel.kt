// ViewModel backing SplashFragment — drives the legacy guide-seeding pipeline shown on first
// launch. Orchestrates a multi-step sequence: dump chapters → dump charts → dump subchapters
// → dumpHTMLInfo → bindHtmlWithChapter, with each step triggered by observing the previous
// step's LiveData result in SplashFragment.
//
// taskFlowEvent (Flow<Callback>): one-shot channel events consumed by SplashFragment to advance
//   the seeding sequence. Emits InsertHTMLInfoComplete and InsertGlobalSearchInfoComplete.
// dumpChartDataObserve / dumpSubChapterDataObserver: boolean guards used by SplashFragment to
//   ensure each LiveData observer fires only once and does not re-trigger on re-subscription.
//
// bindHtmlWithChapter(): final seeding step. Joins chapters, subchapters, and chart metadata
//   with their plain-text HTML bodies (from HtmlInfoEntity) to build GlobalSearchEntity rows,
//   then inserts them inside a Room transaction so global search is fully indexed.
//
// Note: FAMainViewModel.purgeAndSeedFromAssets() supersedes this pipeline for new installs.
// Related files: SplashFragment, Repository, Database, GlobalSearchDao, HtmlInfoDao.
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
class FASplashViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {

    var dumpChartDataObserve = true
    var dumpSubChapterDataObserver = true
    private val taskFlowChannel = Channel<Callback>()
    val taskFlowEvent = taskFlowChannel.receiveAsFlow()

    fun dumpChapterData(data: List<ChapterEntity>) = repo.dumpChapterInfo(data).asLiveData()

    fun dumpChartData(data: List<ChartEntity>) = repo.dumpChartInfo(data).asLiveData()

    fun dumpSubChapterData(data: List<SubChapterEntity>) =
        repo.dumpSubChapterInfo(data).asLiveData()


    /**
     * Inserts pre-built [HtmlInfoEntity] records (plain-text extractions of guide HTML pages)
     * into the database, then signals [Callback.InsertHTMLInfoComplete] to SplashFragment so
     * the next seeding step (bindHtmlWithChapter) can begin.
     */
    fun dumpHTMLInfo(data: ArrayList<HtmlInfoEntity>) = viewModelScope.launch {
        repo.db.htmlInfoDao().insert(data)
        taskFlowChannel.send(Callback.InsertHTMLInfoComplete)
    }

    /**
     * Builds and inserts [GlobalSearchEntity] rows by joining subchapter/chart metadata with
     * the corresponding HtmlInfoEntity plain-text body. This populates the FTS search table.
     * Runs inside a Room transaction; emits [Callback.InsertGlobalSearchInfoComplete] when done.
     */
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