package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentMainFirst6ChartItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity

class FAMainFirst6ChartAdapter :
    ListAdapter<ChartEntity, FAMainFirst6ChartAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<ChartEntity>() {
        override fun areItemsTheSame(oldItem: ChartEntity, newItem: ChartEntity) =
            newItem.subChapterId == oldItem.subChapterId

        override fun areContentsTheSame(oldItem: ChartEntity, newItem: ChartEntity) =
            oldItem == newItem
    }


    inner class ViewHolder(private val first6ChartItemBinding: FragmentMainFirst6ChartItemBinding) :
        RecyclerView.ViewHolder(first6ChartItemBinding.root) {

        fun onBinding(chapterEntity: ChartEntity) = first6ChartItemBinding.apply {
            button.text = chapterEntity.subChapterText
        }

        init {
            first6ChartItemBinding.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentList = currentList[adapterPosition]


                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentMainFirst6ChartItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position))
    }
}