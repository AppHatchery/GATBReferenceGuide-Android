// ViewModel backing MainFragment — the guide's primary navigation hub (chapter/chart lists).
// Also owns all guide seeding and content-update logic, making it the central coordinator for
// the guide's data lifecycle.
//
// Reactive state: getChapter / getChart (LiveData) expose the full chapter and chart lists.
// title (MutableLiveData<String>): drives the toolbar title from any fragment.
// taskFlowEvent (Flow<Callback>): one-shot events signalling seeding milestones
//   (InsertHTMLInfoComplete, InsertGlobalSearchInfoComplete) consumed by MainFragment.
//
// Guide seeding: purgeAndSeedFromAssets() is the authoritative first-install / content-update
//   entry point. It runs a single atomic Room transaction: purge all content tables, then insert
//   chapters, subchapters, charts, and HTML info from bundled JSON/asset files, then rebuild the
//   global search FTS table via rebuildGlobalSearchLocked(). Prefer this over the legacy
//   dumpChapterData / dumpChartData / dumpSubChapterData / bindHtmlWithChapter pipeline.
//
// Content update: downloadAndSavePage() / checkAndUpdatePage() download the live district-
//   coordinator appendix page from the web and save it to filesDir, replacing the bundled copy.
//   MD5 hashing guards against redundant re-downloads. checkTriggerValue() reads a Firebase
//   Remote Config integer to coordinate update triggers across app versions.
//
// Related files: MainFragment, Repository, Database, GlobalSearchDao, HtmlInfoDao, PAGES_DIR.
package org.apphatchery.gatbreferenceguide.ui.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apphatchery.gatbreferenceguide.db.entities.*
import org.apphatchery.gatbreferenceguide.db.repositories.Repository
import org.apphatchery.gatbreferenceguide.utils.EXTENSION
import org.apphatchery.gatbreferenceguide.utils.PAGES_DIR
import org.apphatchery.gatbreferenceguide.utils.html2text
import org.apphatchery.gatbreferenceguide.utils.readJsonFromAssetToString
import org.apphatchery.gatbreferenceguide.utils.removeSlash
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class FAMainViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {

    val title = MutableLiveData<String>()

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

    suspend fun purgeData() {
        repo.purgeData()
    }

    /**
     * One-shot initialization for first install / app update.
     *
     * This avoids fragment-lifecycle-dependent seeding chains (LiveData observers) that can be
     * interrupted when users navigate quickly.
     */
    suspend fun purgeAndSeedFromAssets(context: Context) {
        val initId = System.currentTimeMillis()
        val t0 = System.nanoTime()
        Log.i("INIT_SEED", "[$initId] start purge+seed from assets")
        val charts: List<ChartEntity> = parseAssetJson(context, "chart.json")
        val chapters: List<ChapterEntity> = parseAssetJson(context, "chapter.json")
        val subChapters: List<SubChapterEntity> = parseAssetJson(context, "subchapter.json")
        val htmlInfo: List<HtmlInfoEntity> = extractHtmlInfoFromAssets(context)

        repo.db.withTransaction {
            // Purge + seed must be a single atomic transaction.
            // Database.purgeData() not called here because it starts its own transaction.
            repo.db.chapterDao().deleteAll()
            repo.db.chartDao().deleteAll()
            repo.db.subChapterDao().deleteAll()
            repo.db.htmlInfoDao().deleteAll()
            repo.db.globalSearchDao().deleteAll()

            repo.db.chapterDao().insert(chapters)
            repo.db.subChapterDao().insert(subChapters)
            repo.db.chartDao().insert(charts)
            repo.db.htmlInfoDao().insert(htmlInfo)

            rebuildGlobalSearchLocked()
        }

        val chapterCount = repo.db.chapterDao().count()
        val subChapterCount = repo.db.subChapterDao().count()
        val chartCount = repo.db.chartDao().count()
        val htmlCount = repo.db.htmlInfoDao().count()
        val globalSearchCount = repo.db.globalSearchDao().count()
        val ms = (System.nanoTime() - t0) / 1_000_000
        Log.i(
            "INIT_SEED",
            "[$initId] done in ${ms}ms counts: chapters=$chapterCount subChapters=$subChapterCount charts=$chartCount html=$htmlCount globalSearch=$globalSearchCount"
        )
    }

    /**
     * Reads a bundled JSON asset file and deserialises it into a typed list using Gson.
     * Throws [IllegalStateException] if the asset is missing or fails to parse, so callers
     * fail loudly on first install rather than silently seeding an empty database.
     */
    private inline fun <reified T> parseAssetJson(context: Context, fileName: String): List<T> {
        val json = context.readJsonFromAssetToString(fileName)
            ?: throw IllegalStateException("Missing asset json: $fileName")
        val type = object : TypeToken<List<T>>() {}.type
        return Gson().fromJson<List<T>>(json, type)
            ?: throw IllegalStateException("Failed to parse asset json: $fileName")
    }

    /**
     * Walks the assets/pages directory, strips HTML tags via html2text(), and builds
     * [HtmlInfoEntity] records that power global search full-text indexing.
     * The "GA TB Reference Guide" boilerplate is stripped from each page's text so
     * search results are not polluted by the repeated app title string.
     */
    private fun extractHtmlInfoFromAssets(context: Context): List<HtmlInfoEntity> {
        val results = ArrayList<HtmlInfoEntity>()
        context.assets.list(PAGES_DIR.removeSlash())?.forEach { entry ->
            val file = PAGES_DIR + entry
            var fileName = file.replace(EXTENSION, "")
            fileName = fileName.replace(PAGES_DIR, "")
            results.add(
                HtmlInfoEntity(
                    fileName,
                    context.html2text(file).replace("GA TB Reference Guide", "")
                )
            )
        }
        return results
    }

    /** Must be called inside a DB transaction. */
    private suspend fun rebuildGlobalSearchLocked() {
        val globalSearch = ArrayList<GlobalSearchEntity>()

        repo.db.subChapterDao().getSubChapterBindChapterSuspended().forEach { data ->
            data.subChapterEntity.forEach { sub ->
                globalSearch.add(
                    GlobalSearchEntity(
                        data.chapterEntity.chapterTitle,
                        sub.subChapterTitle,
                        javaClass.name,
                        sub.url,
                        sub.chapterId,
                        sub.subChapterId,
                    )
                )
            }
        }

        repo.db.chartDao().getChartAndSubChapterSuspend().forEach { row ->
            globalSearch.add(
                GlobalSearchEntity(
                    row.chartEntity.chartTitle,
                    row.subChapterEntity.subChapterTitle,
                    javaClass.name,
                    row.chartEntity.id,
                    row.subChapterEntity.chapterId,
                    row.subChapterEntity.subChapterId,
                    true,
                    row.chartEntity.id
                )
            )
        }

        val htmlByFileName = repo.db.htmlInfoDao()
            .getHtmlInfoEntitySuspended()
            .associate { it.fileName to it.htmlText }

        val complete = globalSearch.mapNotNull { entry ->
            val html = htmlByFileName[entry.fileName] ?: return@mapNotNull null
            GlobalSearchEntity(
                entry.searchTitle,
                entry.subChapter,
                html,
                entry.fileName,
                entry.chapterId,
                entry.subChapterId,
                entry.isChart,
                entry.chartId
            )
        }

        repo.db.globalSearchDao().insert(complete)
    }


    fun dumpChapterData(data: List<ChapterEntity>) = repo.dumpChapterInfo(data).asLiveData()

    fun dumpChartData(data: List<ChartEntity>) = repo.dumpChartInfo(data).asLiveData()

    fun dumpSubChapterData(data: List<SubChapterEntity>) =
        repo.dumpSubChapterInfo(data).asLiveData()


    fun dumpHTMLInfo(data: ArrayList<HtmlInfoEntity>) = viewModelScope.launch {
        repo.db.htmlInfoDao().insert(data)
        taskFlowChannel.send(Callback.InsertHTMLInfoComplete)
    }


    /**
     * Legacy global search seeding pipeline (used before purgeAndSeedFromAssets was introduced).
     * Joins subchapter+chapter rows with their corresponding HtmlInfoEntity plain-text bodies,
     * then inserts the combined GlobalSearchEntity records inside a transaction.
     * Emits [Callback.InsertGlobalSearchInfoComplete] on completion.
     * Prefer [purgeAndSeedFromAssets] for new installs and content updates.
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
                        it.subChapterId,
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
                    true,
                    it.chartEntity.id

                )
            )
            Log.d("CHART_DATA1", "Adding chart entity: ${it.chartEntity.chartTitle}")
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
                            globalSearch.isChart,
                            globalSearch.chartId
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

    /**
     * Reads the "update_value" integer from Firebase Remote Config and persists it locally.
     * Used to coordinate content-update triggers: the app compares the remote value to a locally
     * stored value to decide whether a fresh content download is required on next launch.
     */
    private fun checkTriggerValue(remoteConfig: FirebaseRemoteConfig, context: Context){
        val sharedPrefs = context.getSharedPreferences("KEY_UPDATE_VALUE", Context.MODE_PRIVATE)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener{task ->
                val initUpdateValue = remoteConfig.getLong("update_value").toInt()
                sharedPrefs.edit().putInt("KEY_UPDATE_VALUE", initUpdateValue).apply()
            }
    }

    /**
     * First-time download of the live district TB coordinators appendix page from the web.
     * Guards against re-download with a SharedPreferences boolean flag ("isDownloaded").
     * Downloads the HTML, all CSS/JS dependencies, and the title SVG icon, rewrites all
     * resource URLs to local file paths, then saves everything to filesDir.
     * After saving, re-indexes the page in the HtmlInfo table and triggers checkTriggerValue().
     * Runs on [Dispatchers.IO]; swallows exceptions silently to avoid crashing the app on
     * network failure (falls back to the bundled asset copy).
     */
    fun downloadAndSavePage(
        url: String,
        context: Context
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {

            // Check if content has already been downloaded
            val sharedPrefs = context.getSharedPreferences("DownloadPrefs", Context.MODE_PRIVATE)
            if (sharedPrefs.getBoolean("isDownloaded", false)) {
                return@launch
            }

            val localSvgPath = downloadSvgLocally(context)

            val client = OkHttpClient()

            // Download the main HTML page
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val htmlContent = response.body?.string() ?: throw Exception("Empty response")

            // Generate a unique filename (you might want to use a more robust method)
            val htmlFilename = "15_appendix_district_tb_coordinators_(by_district).html"

            // Save the HTML file
            saveFile(htmlFilename, htmlContent, context)

            // Parse the HTML to find dependencies
            val doc = Jsoup.parse(htmlContent, url)

            // Download and save resources (CSS, JS, images)
            doc.select("link[rel=stylesheet], script[src]").forEach { element ->
                val attribute = when (element.tagName()) {
                    "link" -> "href"
                    "script" -> "src"
                    else -> return@forEach
                }
                val resourceUrl = element.absUrl(attribute)
                if (resourceUrl.isNotEmpty()) {
                    val filename = resourceUrl.split('/').last()
                    try {
                        // Download and save the resource
                        val resourceRequest = Request.Builder().url(resourceUrl).build()
                        val resourceResponse = client.newCall(resourceRequest).execute()
                        val resourceBytes = resourceResponse.body?.bytes()
                        if (resourceBytes != null) {
                            saveFile(filename, resourceBytes, context)
                            element.attr(attribute, filename) // Update path to local
                        }
                    } catch (e: Exception) {
                        Log.e("DownloadResource", "Failed to download $resourceUrl", e)
                    }
                }
            }

            // Download and replace images (including SVGs)
            doc.select("img[src$='ic_title_icon.svg']").forEach { element ->
                // Update only the src attribute
                element.attr("src", "file://${context.filesDir}/$localSvgPath")
            }

            // Save the updated HTML file with local resource paths
            saveFile(htmlFilename, doc.outerHtml(), context)

            // Mark content as downloaded in SharedPreferences
            sharedPrefs.edit().putBoolean("isDownloaded", true).apply()
            checkTriggerValue(FirebaseRemoteConfig.getInstance(), context)

            dumpUpdatedHTMLInfo(context)

            // Notify success on the main thread
            withContext(Dispatchers.Main) {
            }
        } catch (e: Exception) {
        }
    }

    private fun dumpUpdatedHTMLInfo(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val fileName = "15_appendix_district_tb_coordinators_(by_district).html"
            val file = File(context.filesDir, fileName)

            if (!file.exists()) {
                Log.e("DumpHTMLInfo", "File does not exist in filesDir: $fileName")
                return@launch
            }

            val htmlContent = file.readText()
            val extractedText = Jsoup.parse(htmlContent).text()

            // Create entity and insert into Room DB
            val htmlInfoEntity = HtmlInfoEntity(fileName.replace(".html", ""), extractedText)
            repo.db.htmlInfoDao().insert(listOf(htmlInfoEntity))

        } catch (e: Exception) {
            Log.e("DumpHTMLInfo", "Error updating HTML content", e)
        }
    }




    private fun downloadSvgLocally(context: Context): String? {
        val svgUrl = "https://apphatchery.github.io/GA-TB-Reference-Guide-Web/assets/ic_title_icon.SVG"
        val fileName = "ic_title_icon.svg"

        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(svgUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("DownloadSVG", "Failed to download SVG: $svgUrl")
                return null
            }

            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { fos -> fos.write(response.body?.bytes()) }

            return fileName
        } catch (e: Exception) {
            Log.e("DownloadSVG", "Error downloading SVG", e)
            null
        }
    }

    /**
     * Checks whether the remote version of [htmlFilename] differs from the locally saved copy
     * by comparing MD5 hashes. If the content has changed, downloads the updated page and
     * all its assets via [downloadAndModifyHtml], then overwrites the local file in filesDir.
     * This is the ongoing update check (post-first-download); [downloadAndSavePage] handles
     * the initial download.
     */
    fun checkAndUpdatePage(url: String, context: Context, htmlFilename: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                // Fetch the current content from the URL
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val newContent = response.body?.string() ?: throw Exception("Empty response")

                // Compute the hash of the new content
                val newHash = newContent.toByteArray().toMD5()

                // Check if the file exists
                val file = File(context.filesDir, htmlFilename)
                if (file.exists()) {
                    val existingContent = file.readText()
                    val existingHash = existingContent.toByteArray().toMD5()
                    // If content is unchanged, return early
                    if (newHash == existingHash) {
                        return@launch
                    }
                }

                val localSvgPath = downloadSvgLocally(context)

                // Parse the HTML and download assets
                val updatedHtml = downloadAndModifyHtml(newContent, url, context, localSvgPath)

                // Save the updated HTML
                saveFile(htmlFilename, updatedHtml, context)

            } catch (e: Exception) {
                Log.e("CheckUpdate", "Failed to check or update the page", e)
            }
        }

    // Function to download assets and modify HTML references
    private fun downloadAndModifyHtml(htmlContent: String, pageUrl: String, context: Context, localSvgPath: String?): String {
        if (localSvgPath == null) return htmlContent
        val doc = Jsoup.parse(htmlContent, pageUrl)

        // Download and replace CSS links
        doc.select("link[rel=stylesheet]").forEach { element ->
            val cssUrl = element.absUrl("href")
            val localPath = downloadAsset(cssUrl, context)
            if (localPath != null) element.attr("href", localPath)
        }

        // Download and replace images (including SVGs)
        doc.select("img[src$='ic_title_icon.svg']").forEach { element ->
            // Update only the src attribute
            element.attr("src", "file://${context.filesDir}/$localSvgPath")
        }

        // Download and replace script files
        doc.select("script[src]").forEach { element ->
            val jsUrl = element.absUrl("src")
            val localPath = downloadAsset(jsUrl, context)
            if (localPath != null) element.attr("src", localPath)
        }

        return doc.html()
    }


    // Function to download a single asset
    private fun downloadAsset(url: String, context: Context, isSvg: Boolean = false): String? {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("DownloadAsset", "Failed to download: $url")
                return null
            }

            var fileName = url.substringAfterLast("/")

            // Ensure the correct file extension for SVGs
            if (isSvg && !fileName.endsWith(".svg", ignoreCase = true)) {
                fileName = fileName.substringBeforeLast(".") + ".svg"
            }

            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { fos -> fos.write(response.body?.bytes()) }

            return fileName
        } catch (e: Exception) {
            Log.e("DownloadAsset", "Error downloading asset: $url", e)
            null
        }
    }

    // Extension function to compute MD5 hash of a ByteArray
    private fun ByteArray.toMD5(): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(this)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Private helper method to save files
    private fun saveFile(filename: String, content: String, context: Context) {
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { fos -> fos.write(content.toByteArray()) }
    }

    // Overloaded method to save byte arrays
    private fun saveFile(filename: String, content: ByteArray, context: Context) {
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { fos -> fos.write(content) }
    }


    sealed class Callback {
        object InsertHTMLInfoComplete : Callback()
        object InsertGlobalSearchInfoComplete : Callback()
    }
}