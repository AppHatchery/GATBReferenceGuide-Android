package org.apphatchery.gatbreferenceguide.ui.viewmodels

import android.content.Context
import android.util.Log
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
                Log.d("skibidi", "Content already downloaded, skipping...")
                return@launch
            }

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
            doc.select("link[rel=stylesheet], script[src], img[src]").forEach { element ->
                val attribute = when (element.tagName()) {
                    "link" -> "href"
                    "script" -> "src"
                    "img" -> "src"
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

            // Save the updated HTML file with local resource paths
            saveFile(htmlFilename, doc.outerHtml(), context)

            // Mark content as downloaded in SharedPreferences
            sharedPrefs.edit().putBoolean("isDownloaded", true).apply()

            // Notify success on the main thread
            withContext(Dispatchers.Main) {
                Log.d("skibidi", "downloadAndSavePage: ")
            }
        } catch (e: Exception) {
            Log.e("skibidi", "Failed to download page", e)
        }
    }

    fun checkAndUpdatePage(url: String, context: Context, htmlFilename: String) = viewModelScope.launch(Dispatchers.IO) {
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
                // Read existing file content and compute its hash
                val existingContent = file.readText()
                val existingHash = existingContent.toByteArray().toMD5()

                // Compare hashes
                if (newHash == existingHash) {
                    Log.d("CheckUpdate", "Content has not changed.")
                    return@launch
                }
            }

            // Save the new content if the file does not exist or the content has changed
            saveFile(htmlFilename, newContent, context)
            Log.d("CheckUpdate", "Content updated successfully.")

        } catch (e: Exception) {
            Log.e("CheckUpdate", "Failed to check or update the page", e)
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