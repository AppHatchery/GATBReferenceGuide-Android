package org.apphatchery.gatbreferenceguide.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.*
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactViewPagerRecyclerViewBinding
import org.apphatchery.gatbreferenceguide.databinding.FragmentSavedViewPagerRecyclerViewBinding
import org.apphatchery.gatbreferenceguide.db.data.ViewPagerData
import org.apphatchery.gatbreferenceguide.ui.fragments.ContactFragment
import org.apphatchery.gatbreferenceguide.ui.fragments.SavedFragment
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAContactViewModel
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASavedViewModel

class FAContactViewPagerAdapter(
    private val viewModel: FAContactViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) :
    ListAdapter<ViewPagerData, FAContactViewPagerAdapter.ViewHolder>(DiffUtilCallBack()) {


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


    inner class ViewHolder(private val bind: FragmentContactViewPagerRecyclerViewBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun onBind(viewPagerData: ViewPagerData) = bind.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = viewPagerData.recyclerViewAdapter
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentContactViewPagerRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = currentList[position]
        holder.onBind(view)
    }
}