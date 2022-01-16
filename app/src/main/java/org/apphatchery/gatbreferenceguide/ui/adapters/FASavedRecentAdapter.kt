package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity

class FASavedRecentAdapter :
    ListAdapter<RecentEntity, FASavedRecentAdapter.ViewHolder>(DiffUtilCallBack()) {

    class DiffUtilCallBack : DiffUtil.ItemCallback<RecentEntity>() {
        override fun areItemsTheSame(oldItem: RecentEntity, newItem: RecentEntity) =
            newItem.id == oldItem.id

        override fun areContentsTheSame(oldItem: RecentEntity, newItem: RecentEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((RecentEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }

    private var onItemClickListAdapter: ((RecentEntity) -> Unit)? = null


    inner class ViewHolder(private val bind: FragmentWithRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(data: RecentEntity) =
            bind.apply {
                textView.text = data.title
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
        holder.onBinding(getItem(position))
    }
}