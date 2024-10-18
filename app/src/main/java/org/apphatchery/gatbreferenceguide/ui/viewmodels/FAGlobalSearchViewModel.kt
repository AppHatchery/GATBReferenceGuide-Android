package org.apphatchery.gatbreferenceguide.ui.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.db.Database
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FAGlobalSearchViewModel @Inject constructor(
    private val db: Database
) : ViewModel() {

    val searchQuery = MutableStateFlow("")


    private val validSearchFlow = searchQuery
        .map { query ->
            query.split(Regex("[\\s.,]+"))
                .filter { it.isNotEmpty() }
                .joinToString(" ")
        }


    private val taskFlow = validSearchFlow
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flow { emit(emptyList()) }
            } else {
                val searchTerms = query.split(" ")
                    .filter { it.isNotEmpty() }
                val formattedQuery = searchTerms.joinToString(separator = " OR ") { "*$it*" }
                db.globalSearchDao().getGlobalSearchEntity(formattedQuery)
            }
        }

    val getGlobalSearchEntity = taskFlow.asLiveData()
    fun getSubChapterById(id: String) = db.subChapterDao().getSubChapterById(id).asLiveData()

}
