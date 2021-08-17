package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.db.repositories.FASplashRepo
import javax.inject.Inject

@HiltViewModel
class FASplashViewModel @Inject constructor(
    private val repo: FASplashRepo
) : ViewModel() {

    fun dumpChapterData(data: List<ChapterEntity>) = repo.dumpChapterInfo(data).asLiveData()

    fun dumpChartData(data: List<ChartEntity>) = repo.dumpChartInfo(data).asLiveData()

    fun dumpSubChapterData(data: List<SubChapterEntity>) =
        repo.dumpSubChapterInfo(data).asLiveData()


}