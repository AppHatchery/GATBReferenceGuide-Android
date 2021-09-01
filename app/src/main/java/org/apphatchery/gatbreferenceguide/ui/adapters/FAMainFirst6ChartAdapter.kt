package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentMainFirst6ChartItemBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity

class FAMainFirst6ChartAdapter :
    ListAdapter<ChartAndSubChapter, FAMainFirst6ChartAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<ChartAndSubChapter>() {
        override fun areItemsTheSame(oldItem: ChartAndSubChapter, newItem: ChartAndSubChapter) =
            newItem.chartEntity.id == oldItem.chartEntity.id

        override fun areContentsTheSame(
            oldItem: ChartAndSubChapter,
            newItem: ChartAndSubChapter
        ) =
            oldItem == newItem
    }


    fun itemClickCallback(listener: ((ChartAndSubChapter) -> Unit)) {
        onItemClickListAdapter = listener
    }

    var onItemClickListAdapter: ((ChartAndSubChapter) -> Unit)? = null

    inner class ViewHolder(private val first6ChartItemBinding: FragmentMainFirst6ChartItemBinding) :
        RecyclerView.ViewHolder(first6ChartItemBinding.root) {

        fun onBinding(chapterEntity: ChartAndSubChapter) = first6ChartItemBinding.apply {
            button.text = chapterEntity.chartEntity.chartTitle
        }

        init {
            first6ChartItemBinding.button.setOnClickListener {
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
        FragmentMainFirst6ChartItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position))
    }
}