package org.apphatchery.gatbreferenceguide.ui.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
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

    // Download and save framer page
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

            // Notify success on the main thread
            withContext(Dispatchers.Main) {
            }
        } catch (e: Exception) {
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