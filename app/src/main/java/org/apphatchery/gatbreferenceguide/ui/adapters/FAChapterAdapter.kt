package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentChapterAndSubChapterItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.utils.ROMAN_NUMERALS

class FAChapterAdapter :
    ListAdapter<ChapterEntity, FAChapterAdapter.ViewHolder>(DiffUtilCallBack()) {


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


    inner class ViewHolder(private val fragmentChapterAndSubChapterItemBinding: FragmentChapterAndSubChapterItemBinding) :
        RecyclerView.ViewHolder(fragmentChapterAndSubChapterItemBinding.root) {

        fun onBinding(chapterEntity: ChapterEntity, index: Int) =
            fragmentChapterAndSubChapterItemBinding.apply {
                (ROMAN_NUMERALS[index].uppercase() + ". " + chapterEntity.chapterTitle).also {
                    textView.text = it
                }


            }

        init {
            fragmentChapterAndSubChapterItemBinding.root.setOnClickListener {
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
        FragmentChapterAndSubChapterItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position), position)
    }
}