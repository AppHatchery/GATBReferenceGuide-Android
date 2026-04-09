// DAO for HtmlInfoEntity — the pre-rendered HTML text cache for guide pages in the TB app.
// Each row maps a fileName (matching the asset path used by BodyFragment's WebView) to its full
// HTML string. Caching content here lets BodyFragment load pages from Room instead of re-parsing
// raw asset files on every navigation, which is critical for older devices.
//
// insert() uses REPLACE (not IGNORE) because guide content updates must overwrite stale HTML.
// This means a new app version can silently update a page's HTML in the Room cache on first launch
// without needing to delete it first — unlike ChartDao or SubChapterDao which use IGNORE + deleteAll.
//
// Two read variants exist for the same reason as ChartDao: getHtmlInfoEntity() is a Flow for live
// UI observation, while getHtmlInfoEntitySuspended() is a one-shot suspend for the startup seeding
// path where subscribing to a long-lived Flow is inappropriate.
//
// Related: HtmlInfoEntity, BodyFragment, FASplashViewModel, Database.
package org.apphatchery.gatbreferenceguide.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.apphatchery.gatbreferenceguide.db.entities.HtmlInfoEntity

@Dao
interface HtmlInfoDao {

    /** Inserts or replaces HTML cache entries. REPLACE strategy means guide content updates are
     *  automatically applied on the next reseed without a prior deleteAll() call. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: List<HtmlInfoEntity>)

    @Delete
    suspend fun delete(data: HtmlInfoEntity)

    /** Live Flow of all cached HTML entries, suitable for reactive UI observation. */
    @Query("SELECT  * FROM  HtmlInfoEntity")
    fun getHtmlInfoEntity(): Flow<List<HtmlInfoEntity>>

    /** One-shot suspend read; used during startup to check whether the HTML cache is populated
     *  before deciding whether to seed from assets, avoiding a redundant Flow subscription. */
    @Query("SELECT  * FROM  HtmlInfoEntity")
   suspend fun getHtmlInfoEntitySuspended(): List<HtmlInfoEntity>

    @Query("DELETE FROM HtmlInfoEntity")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM HtmlInfoEntity")
    suspend fun count(): Int

}