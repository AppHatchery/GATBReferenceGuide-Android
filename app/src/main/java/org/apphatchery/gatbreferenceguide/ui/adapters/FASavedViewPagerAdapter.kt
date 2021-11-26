package org.apphatchery.gatbreferenceguide.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.*
import org.apphatchery.gatbreferenceguide.databinding.FragmentSavedViewPagerRecyclerViewBinding
import org.apphatchery.gatbreferenceguide.db.data.ViewPagerData
import org.apphatchery.gatbreferenceguide.ui.fragments.SavedFragment
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASavedViewModel

class FASavedViewPagerAdapter(
    private val viewModel: FASavedViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) :
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


        init {
            viewModel.savedItemCount.asLiveData().observe(viewLifecycleOwner) {

                with(bind) {

                    recyclerView.isVisible = it.itemCount == -1 || it.itemCount > 0

                    includeFragmentNoRecent.root.visibility = View.GONE
                    includeFragmentNoBookmark.root.visibility = View.GONE
                    includeFragmentNoNote.root.visibility = View.GONE

                    when (it.savedType) {
                        SavedFragment.SavedType.RECENT -> {
                            includeFragmentNoRecent.root.isVisible =
                                it.itemCount < 1
                        }
                        SavedFragment.SavedType.BOOKMARK -> includeFragmentNoBookmark.root.isVisible =
                            it.itemCount < 1
                        SavedFragment.SavedType.NOTES -> includeFragmentNoNote.root.isVisible =
                            it.itemCount < 1
                    }
                }
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