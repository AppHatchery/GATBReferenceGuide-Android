package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentChapterAndSubChapterItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.utils.ALPHABET

class FASubChapterAdapter :
    ListAdapter<SubChapterEntity, FASubChapterAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<SubChapterEntity>() {
        override fun areItemsTheSame(oldItem: SubChapterEntity, newItem: SubChapterEntity) =
            newItem.subChapterId == oldItem.subChapterId

        override fun areContentsTheSame(oldItem: SubChapterEntity, newItem: SubChapterEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((SubChapterEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }

    var onItemClickListAdapter: ((SubChapterEntity) -> Unit)? = null


    inner class ViewHolder(private val fragmentChapterAndSubChapterItemBinding: FragmentChapterAndSubChapterItemBinding) :
        RecyclerView.ViewHolder(fragmentChapterAndSubChapterItemBinding.root) {

        fun onBinding(chapterEntity: SubChapterEntity, index: Int) =
            fragmentChapterAndSubChapterItemBinding.apply {
                (ALPHABET[index].uppercase() + ". " + chapterEntity.subChapterTitle).also {
                    textView.text = it
                }
            }

        init {
            fragmentChapterAndSubChapterItemBinding.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentList = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        it(currentList)
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