package org.apphatchery.gatbreferenceguide.db.repositories

import androidx.room.withTransaction
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.utils.networkBoundResource
import javax.inject.Inject


class Repository @Inject constructor(
    val db: Database
) {

    private val chapterDao = db.chapterDao()
    private val subChapterDao = db.subChapterDao()
    private val chartDao = db.chartDao()


    fun dumpChapterInfo(data: List<ChapterEntity>) = networkBoundResource(
        query = { chapterDao.getChapterEntity() },
        fetch = { data },
        saveToDb = {
            db.withTransaction {
                chapterDao.insert(it)
            }
        }
    )


    fun dumpSubChapterInfo(data: List<SubChapterEntity>) = networkBoundResource(
        query = { subChapterDao.getSubChapterEntity() },
        fetch = { data },
        saveToDb = {
            db.withTransaction {
                subChapterDao.insert(it)
            }
        }
    )

    fun dumpChartInfo(data: List<ChartEntity>) = networkBoundResource(
        query = { chartDao.getChartAndSubChapter() },
        fetch = { data },
        saveToDb = {
            db.withTransaction {
                chartDao.insert(it)
            }
        }
    )

}