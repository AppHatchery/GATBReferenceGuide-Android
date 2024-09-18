package org.apphatchery.gatbreferenceguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FAGlobalSearchViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    private val taskFlow = searchQuery.flatMapLatest {
        val searchQuery =
            if (it.isEmpty()) "e" else it.replace(Regex.fromLiteral("\""), "\"\"").trim()
        db.globalSearchDao().getGlobalSearchEntity("*$searchQuery*")
    }

    val getGlobalSearchEntity = taskFlow.asLiveData()
    fun getSubChapterById(id: String) = db.subChapterDao().getSubChapterById(id).asLiveData()

    //changed here

//    fun getChapters(keyword: String = "") = db.globalSearchDao().getChapters("*$keyword*").asLiveData()
//    fun getCharts(keyword: String = "") = db.globalSearchDao().getCharts("*$keyword*").asLiveData()



//    fun purgeData() {
//        viewModelScope.launch {
//            db.withTransaction {
//                db.chapterDao().deleteAll()
//                db.chartDao().deleteAll()
//                db.subChapterDao().deleteAll()
//                db.htmlInfoDao().deleteAll()
//                db.globalSearchDao().deleteAll()
//            }
//        }
//    }
//
//    fun performSearch(query: String) {
//        searchQuery.value = query
//    }

//    val searchQuery = MutableStateFlow("")
//
//    private val taskFlow = searchQuery.flatMapLatest {
//        val searchQuery =
//            if (it.isEmpty()) "e" else it.replace(Regex.fromLiteral("\""), "\"\"").trim()
//        db.globalSearchDao().getGlobalSearchEntity("*$searchQuery*")
//    }
//
//    val getGlobalSearchEntity = taskFlow.asLiveData()
//    fun getSubChapterById(id: String) = db.subChapterDao().getSubChapterById(id).asLiveData()
}