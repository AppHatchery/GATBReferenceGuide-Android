package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewItemBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.utils.ALPHABET

class FAChartAdapter :
    ListAdapter<ChartAndSubChapter, FAChartAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<ChartAndSubChapter>() {
        override fun areItemsTheSame(oldItem: ChartAndSubChapter, newItem: ChartAndSubChapter) =
            newItem.chartEntity.id == oldItem.chartEntity.id

        override fun areContentsTheSame(oldItem: ChartAndSubChapter, newItem: ChartAndSubChapter) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((ChartAndSubChapter) -> Unit)) {
        onItemClickListAdapter = listener
    }

    var onItemClickListAdapter: ((ChartAndSubChapter) -> Unit)? = null


    inner class ViewHolder(private val bind: FragmentWithRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(item: ChartAndSubChapter, index: Int) =
            bind.apply {
                (ALPHABET[index].uppercase() + ". " + item.chartEntity.chartTitle).also {
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