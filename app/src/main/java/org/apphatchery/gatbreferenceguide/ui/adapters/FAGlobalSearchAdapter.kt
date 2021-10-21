package org.apphatchery.gatbreferenceguide.ui.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
 import org.apphatchery.gatbreferenceguide.databinding.FragmentGlobalSearchItemBinding
import org.apphatchery.gatbreferenceguide.db.data.GlobalSearchWithMatchInfo
 import javax.inject.Inject

class FAGlobalSearchAdapter @Inject constructor(
) : ListAdapter<GlobalSearchWithMatchInfo, FAGlobalSearchAdapter.ViewHolder>(DiffUtilCallBack()) {

    var searchQuery: String = ""


    class DiffUtilCallBack : DiffUtil.ItemCallback<GlobalSearchWithMatchInfo>() {
        override fun areItemsTheSame(oldItem: GlobalSearchWithMatchInfo, newItem: GlobalSearchWithMatchInfo) =
            newItem.globalSearchEntity.fileName == oldItem.globalSearchEntity.fileName

        override fun areContentsTheSame(oldItem: GlobalSearchWithMatchInfo, newItem: GlobalSearchWithMatchInfo) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((GlobalSearchWithMatchInfo) -> Unit)) {
        onItemClickListAdapter = listener
    }


    private var onItemClickListAdapter: ((GlobalSearchWithMatchInfo) -> Unit)? = null

    private fun prepSearchQuery(textInBody: String) =
        textInBody.subSequence(getSearchStartPosition(textInBody), textInBody.length).toString()

    private fun getSearchStartPosition(textInBody: String) =
        if (textInBody.lowercase().indexOf(searchQuery) == -1) 0
        else textInBody.lowercase().indexOf(searchQuery)

    inner class ViewHolder(private val fragmentGlobalSearchItemBinding: FragmentGlobalSearchItemBinding) :
        RecyclerView.ViewHolder(fragmentGlobalSearchItemBinding.root) {

        fun onBinding(globalSearchEntity: GlobalSearchWithMatchInfo, index: Int) =
            fragmentGlobalSearchItemBinding.apply {

//                textInBody.text = globalSearchEntity.textInBody
//                searchTitle.text = globalSearchEntity.searchTitle
//                subChapter.text = globalSearchEntity.subChapter

                setSpannableString(prepSearchQuery(globalSearchEntity.globalSearchEntity.textInBody)) {
                    textInBody.text = this
                }

                setSpannableString(globalSearchEntity.globalSearchEntity.searchTitle) {
                    searchTitle.text = this
                }

                setSpannableString(globalSearchEntity.globalSearchEntity.subChapter) {
                    subChapter.text = this
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