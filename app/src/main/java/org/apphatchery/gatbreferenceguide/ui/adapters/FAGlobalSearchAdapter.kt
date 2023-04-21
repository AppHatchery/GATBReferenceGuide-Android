package org.apphatchery.gatbreferenceguide.ui.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentGlobalSearchItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity
import javax.inject.Inject

class FAGlobalSearchAdapter @Inject constructor(
) : ListAdapter<GlobalSearchEntity, FAGlobalSearchAdapter.ViewHolder>(DiffUtilCallBack()) {

    var searchQuery: String = ""


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
        if (textInBody.lowercase().indexOf(searchQuery) == -1) 0
        else textInBody.lowercase().indexOf(searchQuery)

    inner class ViewHolder(private val fragmentGlobalSearchItemBinding: FragmentGlobalSearchItemBinding) :
        RecyclerView.ViewHolder(fragmentGlobalSearchItemBinding.root) {

        fun onBinding(globalSearchEntity: GlobalSearchEntity, index: Int) =
            fragmentGlobalSearchItemBinding.apply {

                searchTitle.text = HtmlCompat.fromHtml(globalSearchEntity.searchTitle,FROM_HTML_MODE_LEGACY)
                subChapter.text = HtmlCompat.fromHtml(globalSearchEntity.subChapter,FROM_HTML_MODE_LEGACY)
                textInBody.text = HtmlCompat.fromHtml(globalSearchEntity.textInBody, FROM_HTML_MODE_LEGACY)

                val bodyWithTags = globalSearchEntity.textInBody
                val pattern = ".*<span style='background-color: yellow; color: black; font-weight: bold;'>(.*?)</span>.*".toRegex()
                val matchResult = pattern.find(bodyWithTags)
                val extractedSearchValue = matchResult?.groupValues?.get(1) ?: ""

                val locationOfTarget =  textInBody.text.indexOf(extractedSearchValue)
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

        init {
            fragmentGlobalSearchItemBinding.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
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
        FragmentGlobalSearchItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position), position)
    }


}