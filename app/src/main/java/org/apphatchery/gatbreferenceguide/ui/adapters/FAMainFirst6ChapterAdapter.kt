package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentMainFirst6ChapterItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity

class FAMainFirst6ChapterAdapter :
    ListAdapter<ChapterEntity, FAMainFirst6ChapterAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<ChapterEntity>() {
        override fun areItemsTheSame(oldItem: ChapterEntity, newItem: ChapterEntity) =
            newItem.chapterId == oldItem.chapterId

        override fun areContentsTheSame(oldItem: ChapterEntity, newItem: ChapterEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((ChapterEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }

    var onItemClickListAdapter: ((ChapterEntity) -> Unit)? = null


    inner class ViewHolder(private val first6ChapterItemBinding: FragmentMainFirst6ChapterItemBinding) :
        RecyclerView.ViewHolder(first6ChapterItemBinding.root) {

        fun onBinding(chapterEntity: ChapterEntity) =
            first6ChapterItemBinding.apply {
                button.text = chapterEntity.chapterTitle
            }


        init {
            first6ChapterItemBinding.button.setOnClickListener {
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
        FragmentMainFirst6ChapterItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position))
    }
}