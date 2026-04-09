// Domain-layer service that coordinates database seeding and querying for the guide's core content.
// ViewModels receive this via Hilt @Inject constructor injection and call it to load chapters,
// subchapters, and charts into the local Room database from bundled JSON asset files.
//
// dumpChapterInfo / dumpSubChapterInfo / dumpChartInfo: each wraps a networkBoundResource() call.
// networkBoundResource() (see utils/NetworkBoundResource.kt) emits the current DB state as a Flow
// immediately, then writes the provided data into the DB inside a transaction. Despite the name,
// no network call is made — "network" here means "external data source" (bundled JSON assets).
// The query() lambda provides the reactive Flow that UI observers receive; saveToDb() does the write.
//
// purgeData(): delegates straight to Database.purgeData(), which clears only seeded-content tables
// atomically. Called by GuideContentUpdater before any of the dump*() methods on content update.
//
// Note: this Repository only covers seeded guide content. User data (bookmarks, notes, contacts)
// is accessed directly through the Database instance in each ViewModel (e.g. FABodyViewModel),
// keeping this class focused on content lifecycle rather than user-state management.
// Related: Database, NetworkBoundResource, GuideContentUpdater, FAMainViewModel, FAChapterViewModel.
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

    suspend fun purgeData() {
        db.purgeData()
    }


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
