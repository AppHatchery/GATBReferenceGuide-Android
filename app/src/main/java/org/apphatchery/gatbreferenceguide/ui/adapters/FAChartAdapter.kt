// ListAdapter rendering the full chart/table list in ChartFragment's RecyclerView.
// Each row displays the title of a ChartAndSubChapter item (chartEntity.chartTitle) using the
// shared FragmentWithRecyclerviewItemBinding layout (a simple single-TextView row).
//
// The ALPHABET constant is imported but not used by this adapter directly — it is used by
// FASubChapterAdapter for alphabetic prefixing. It appears here due to a shared import pattern.
//
// Related files:
//   - ChartAndSubChapter (db/data) — the joined data class combining ChartEntity + SubChapterEntity
//   - FAChartFragment — attaches this adapter and handles itemClickCallback to open chart content
//   - FragmentWithRecyclerviewItemBinding — shared single-row layout used by several adapters
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewItemBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.utils.ALPHABET

class FAChartAdapter :
    ListAdapter<ChartAndSubChapter, FAChartAdapter.ViewHolder>(DiffUtilCallBack()) {


    /**
     * Identifies items by chartEntity.id; compares contents by hashCode so that any field change
     * in ChartAndSubChapter triggers a rebind.
     */
    class DiffUtilCallBack : DiffUtil.ItemCallback<ChartAndSubChapter>() {
        override fun areItemsTheSame(oldItem: ChartAndSubChapter, newItem: ChartAndSubChapter) =
            newItem.chartEntity.id == oldItem.chartEntity.id

        override fun areContentsTheSame(
            oldItem: ChartAndSubChapter,
            newItem: ChartAndSubChapter
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    fun itemClickCallback(listener: ((ChartAndSubChapter) -> Unit)) {
        onItemClickListAdapter = listener
    }

    var onItemClickListAdapter: ((ChartAndSubChapter) -> Unit)? = null


    inner class ViewHolder(private val bind: FragmentWithRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(item: ChartAndSubChapter, index: Int) =
            bind.apply {
                (item.chartEntity.chartTitle).also {
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
