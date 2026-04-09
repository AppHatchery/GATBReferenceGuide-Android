// ListAdapter acting as the ViewPager2 page adapter for the Contacts screen (ContactFragment).
// Each page is a ViewPagerData item that carries a pre-built RecyclerView adapter (e.g.
// FAContactAdapter for public contacts, FAPrivateContactAdapter for private contacts). The
// ViewHolder simply assigns that inner adapter to a GridLayoutManager(span=1) RecyclerView,
// effectively nesting a RecyclerView inside each ViewPager page.
//
// This adapter receives FAContactViewModel and a LifecycleOwner so that future observers (e.g.
// empty-state visibility) can be wired up per-page without leaking the fragment lifecycle.
//
// Related files:
//   - ViewPagerData (db/data) — holds the recyclerViewAdapter reference and optional swipe callback
//   - ContactFragment — builds the ViewPagerData list and submits it to this adapter
//   - FAContactAdapter / FAPrivateContactAdapter — the inner adapters placed on each page
//   - FASavedViewPagerAdapter — parallel adapter for the Saved screen, also uses ViewPagerData
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


    /**
     * Items are considered the same if they wrap the same adapter instance; contents are compared
     * by hashCode since ViewPagerData carries mutable state.
     */
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


    /**
     * Each ViewHolder hosts a single-column RecyclerView whose adapter is supplied via ViewPagerData.
     * Using GridLayoutManager(span=1) gives a standard vertical list while keeping the layout
     * manager creation centralised here rather than in the fragment.
     */
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
