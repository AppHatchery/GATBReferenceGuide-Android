package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewItemBinding
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


    inner class ViewHolder(private val bind: FragmentWithRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(chapterEntity: SubChapterEntity, index: Int) =
            bind.apply {
                (ALPHABET[index].uppercase() + ". " + chapterEntity.subChapterTitle).also {
                    textView.text = it
                }
            }

        init {
            bind.root.setOnClickListener {
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
        FragmentWithRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position), position)
    }
}