// ViewModel backing ChartFragment — displays the guide's diagnostic/treatment chart index.
// Charts are a separate content category from chapters; each chart is linked to a parent
// subchapter for navigation context (ChartAndSubChapter relation).
//
// getChart (LiveData<List<ChartAndSubChapter>>): the full chart list with associated subchapter
//   info, observed by ChartFragment to populate the chart index adapter.
// getChapterInfo(id): one-shot lookup of a ChapterEntity by ID; used by ChartFragment to
//   display the parent chapter name in the toolbar when entering a chart.
//
// Related files: ChartFragment, Database, ChartDao, ChapterDao, ChartAndSubChapter.
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