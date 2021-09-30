package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import org.apphatchery.gatbreferenceguide.databinding.FragmentSavedViewPagerRecyclerViewBinding
import org.apphatchery.gatbreferenceguide.db.data.ViewPagerData

class FASavedViewPagerAdapter :
    ListAdapter<ViewPagerData, FASavedViewPagerAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<ViewPagerData>() {
        override fun areItemsTheSame(
            oldItem: ViewPagerData,
            newItem: ViewPagerData
        ) =
            newItem.recyclerViewAdapter == oldItem.recyclerViewAdapter

        override fun areContentsTheSame(
            oldItem: ViewPagerData,
            newItem: ViewPagerData
        ) =
            oldItem.hashCode() == newItem.hashCode()
    }

    inner class ViewHolder(private val bind: FragmentSavedViewPagerRecyclerViewBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun onBind(viewPagerData: ViewPagerData) = bind.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = viewPagerData.recyclerViewAdapter
            viewPagerData.swipeToDeleteCallback?.let {
                ItemTouchHelper(viewPagerData.swipeToDeleteCallback as ItemTouchHelper.Callback)
                    .attachToRecyclerView(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentSavedViewPagerRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = currentList[position]
        holder.onBind(view)
    }
}