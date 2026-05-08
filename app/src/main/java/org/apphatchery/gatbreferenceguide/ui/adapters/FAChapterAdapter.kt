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

                // Number chapters using their position among numbered chapters only,
                // so inserting an unnumbered item doesn't shift the Roman numerals.
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