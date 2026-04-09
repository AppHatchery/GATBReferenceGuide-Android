// ListAdapter rendering the top-level TB guide chapter list in ChapterFragment's RecyclerView.
// Each row displays the chapter's Roman numeral prefix and title. Chapter ID 18 is a special
// unnumbered chapter (e.g. Abbreviations) that receives no numeral and is always sorted first
// by ChapterDao — this adapter mirrors that by skipping the prefix for ID 18 specifically.
//
// Roman numeral numbering logic: rather than using the adapter position directly, it counts only
// chapters with chapterId != 18 that appear before and including the current position. This keeps
// numbering stable even if the unnumbered chapter is inserted at the top — no other chapter's
// numeral shifts. ROMAN_NUMERALS is a pre-built list from utils/Numbering.kt.
//
// UNNUMBERED_CHAPTER_ID = 18 is a companion const so its purpose is explicit at every usage site.
// If the guide editors ever reassign the unnumbered chapter to a different ID, update this constant
// and the matching CASE expression in ChapterDao.getChapterEntity().
//
// DiffUtilCallBack uses chapterId as the stable identity key and structural (==) equality for
// content comparison, ensuring only changed rows are animated on list updates.
// itemClickCallback: ChapterFragment sets this to navigate to SubChapterFragment via NavController.
// Related: ChapterEntity, ChapterFragment, FAChapterViewModel, ROMAN_NUMERALS (Numbering.kt).
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.utils.ROMAN_NUMERALS

class FAChapterAdapter :
    ListAdapter<ChapterEntity, FAChapterAdapter.ViewHolder>(DiffUtilCallBack()) {

    private companion object {
        const val UNNUMBERED_CHAPTER_ID = 18
    }


    class DiffUtilCallBack : DiffUtil.ItemCallback<ChapterEntity>() {
        override fun areItemsTheSame(oldItem: ChapterEntity, newItem: ChapterEntity) =
            newItem.chapterId == oldItem.chapterId

        override fun areContentsTheSame(oldItem: ChapterEntity, newItem: ChapterEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((ChapterEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }

    private var onItemClickListAdapter: ((ChapterEntity) -> Unit)? = null


    inner class ViewHolder(private val bind: FragmentWithRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(chapterEntity: ChapterEntity, index: Int) =
            bind.apply {
                if (chapterEntity.chapterId == UNNUMBERED_CHAPTER_ID) {
                    textView.text = chapterEntity.chapterTitle
                    return@apply
                }

                // Count only numbered chapters up to this position to get the correct Roman numeral.
                // This prevents the unnumbered chapter (ID 18) from consuming an index slot and
                // offsetting all subsequent chapter numerals.
                val numberedIndex = currentList
                    .subList(0, index + 1)
                    .count { it.chapterId != UNNUMBERED_CHAPTER_ID } - 1

                val roman = ROMAN_NUMERALS.getOrNull(numberedIndex)?.uppercase()
                textView.text = if (roman != null) "$roman. ${chapterEntity.chapterTitle}" else chapterEntity.chapterTitle


            }

        init {
            bind.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentWithRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position), position)
    }
}
