package org.apphatchery.gatbreferenceguide.ui.adapters

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.databinding.FragmentGlobalSearchItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FABodyViewModel
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAGlobalSearchViewModel
import org.apphatchery.gatbreferenceguide.utils.ROMAN_NUMERALS
import org.apphatchery.gatbreferenceguide.utils.searchState
import javax.inject.Inject

class FAGlobalSearchAdapter @Inject constructor(
) : ListAdapter<GlobalSearchEntity, FAGlobalSearchAdapter.ViewHolder>(DiffUtilCallBack()) {

    var searchQuery: String = ""
    var searchQuery_ = MutableStateFlow("")
    private var currentFilter: SearchResultType = SearchResultType.ALL
    private var allItems: List<GlobalSearchEntity> = emptyList()

    enum class SearchResultType {
        ALL, CHARTS, CHAPTERS
    }


    class DiffUtilCallBack : DiffUtil.ItemCallback<GlobalSearchEntity>() {
        override fun areItemsTheSame(oldItem: GlobalSearchEntity, newItem: GlobalSearchEntity) =
            newItem.fileName == oldItem.fileName

        override fun areContentsTheSame(oldItem: GlobalSearchEntity, newItem: GlobalSearchEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((GlobalSearchEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }


    private var onItemClickListAdapter: ((GlobalSearchEntity) -> Unit)? = null

    private fun prepSearchQuery(textInBody: String) =
        textInBody.subSequence(getSearchStartPosition(textInBody), textInBody.length).toString()

    private fun getSearchStartPosition(textInBody: String) =
        if (textInBody.indexOf(searchQuery) == -1) 0
        else textInBody.indexOf(searchQuery)

    fun updateData(newItems: List<GlobalSearchEntity>) {
        allItems = newItems
        filter(currentFilter)
    }

    fun filter(type: SearchResultType) {
        currentFilter = type
        val filteredList = when (type) {
            SearchResultType.ALL -> allItems
            SearchResultType.CHARTS -> allItems.filter { it.isChart }
            SearchResultType.CHAPTERS -> allItems.filter { !it.isChart }
        }
        submitList(filteredList)
    }

    inner class ViewHolder(private val fragmentGlobalSearchItemBinding: FragmentGlobalSearchItemBinding) :
        RecyclerView.ViewHolder(fragmentGlobalSearchItemBinding.root) {

        fun onBinding(globalSearchEntity: GlobalSearchEntity, index: Int) =
            fragmentGlobalSearchItemBinding.apply {
                CoroutineScope(Dispatchers.Unconfined).launch {
                    val searchChapterID = globalSearchEntity.chapterId -1
                    val romanChapterID = ROMAN_NUMERALS[searchChapterID].uppercase()

                    // Check if the result is a chart and update the title accordingly
                    if (globalSearchEntity.isChart) {
                        // Display the chart title for chart results
                        subChapter.text = HtmlCompat.fromHtml(
                            "${globalSearchEntity.searchTitle}",
                            FROM_HTML_MODE_LEGACY
                        )
                        searchTitle.text = HtmlCompat.fromHtml(globalSearchEntity.subChapter,FROM_HTML_MODE_LEGACY)
                    } else {
                        searchTitle.text = "$romanChapterID. ${HtmlCompat.fromHtml(globalSearchEntity.searchTitle,FROM_HTML_MODE_LEGACY)}"
                        subChapter.text = HtmlCompat.fromHtml(globalSearchEntity.subChapter,FROM_HTML_MODE_LEGACY)
                    }
//                searchTitle.text = "$romanChapterID. ${HtmlCompat.fromHtml(globalSearchEntity.searchTitle,FROM_HTML_MODE_LEGACY)}"
//                subChapter.text = HtmlCompat.fromHtml(globalSearchEntity.subChapter,FROM_HTML_MODE_LEGACY)
                    textInBody.text = HtmlCompat.fromHtml(globalSearchEntity.textInBody, FROM_HTML_MODE_LEGACY)
                }.invokeOnCompletion {
                    //var searchTitleNumber = HtmlCompat.fromHtml(globalSearchEntity.,FROM_HTML_MODE_LEGACY)

                    val bodyWithTags = globalSearchEntity.textInBody
                    val pattern = ".*<span style='background-color: yellow; color: black; font-weight: bold;'>(.*?)</span>.*".toRegex()
                    val matchResult = pattern.find(bodyWithTags)
                    val extractedSearchValue = matchResult?.groupValues?.get(1) ?: ""

                    val locationOfTarget = textInBody.text.indexOf(extractedSearchValue)

                    if (locationOfTarget != -1) {
                        textInBody.maxLines = 2
                        textInBody.ellipsize = TextUtils.TruncateAt.MARQUEE
                        textInBody.post {
                            val line = textInBody.layout.getLineForOffset(locationOfTarget)
                            val y = textInBody.layout.getLineTop(line)
                            textInBody.scrollTo(0, y)
                        }
                    }
                }

            }

        init {
            fragmentGlobalSearchItemBinding.root.setOnClickListener {

                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        searchState.enterSearchMode()
                        it(currentClickedItem)
                    }
                }
            }
        }
    }

    private fun setSpannableString(
        spannableString: String,
        spannableTextCallback: SpannableString.() -> Unit
    ) = SpannableString(spannableString).apply {
        try {
            setSpan(
                if (spannableString.contains(searchQuery, true))
                    StyleSpan(Typeface.BOLD) else StyleSpan(Typeface.NORMAL),
                0,
                searchQuery.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTextCallback(this.trim() as SpannableString)
        } catch (e: Exception) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentGlobalSearchItemBinding.inflate(LayoutInflater.from(parent.context))
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position), position)
    }
}
