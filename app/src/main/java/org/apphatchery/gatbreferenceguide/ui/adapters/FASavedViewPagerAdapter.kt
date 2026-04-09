// ListAdapter acting as the ViewPager2 page adapter for the Saved screen (SavedFragment).
// The three pages are Recent, Bookmarks, and Notes — each represented by a ViewPagerData item
// that carries a pre-built inner RecyclerView adapter and an optional swipe callback.
//
// Per-page behaviour in ViewHolder.init:
//   - Observes FASavedViewModel.savedItemCount (a Flow<SavedItemCount>) for the current page.
//   - When itemCount < 1, shows the appropriate empty-state placeholder (includeFragmentNoRecent,
//     includeFragmentNoBookmark, or includeFragmentNoNote) and hides the RecyclerView.
//   - itemCount == -1 is a sentinel meaning "not yet loaded"; the RecyclerView is kept visible
//     to avoid a flash of the empty state on initial load.
//   - All three placeholder views are reset to GONE before the active one is conditionally shown,
//     preventing stale visibility state when ViewHolders are recycled across pages.
//
// Swipe-to-delete: if ViewPagerData.swipeToDeleteCallback is non-null, an ItemTouchHelper is
// attached to the page's RecyclerView, enabling left-swipe deletion (used for Recents/Bookmarks).
//
// Related files:
//   - ViewPagerData (db/data) — carries recyclerViewAdapter + optional swipeToDeleteCallback
//   - SavedFragment — builds the three ViewPagerData entries and submits them here
//   - FASavedViewModel — exposes savedItemCount; SavedType enum matches the three page types
//   - SwipeDecoratorCallback — the concrete swipe callback attached via ViewPagerData
package org.apphatchery.gatbreferenceguide.ui.adapters

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
            // Attach swipe-to-delete helper if this page's data provides one (e.g. Recents, Bookmarks)
            viewPagerData.swipeToDeleteCallback?.let {
                ItemTouchHelper(viewPagerData.swipeToDeleteCallback as ItemTouchHelper.Callback)
                    .attachToRecyclerView(this)
            }
        }


        init {
            // Observe item count to toggle empty-state placeholders per saved type
            viewModel.savedItemCount.asLiveData().observe(viewLifecycleOwner) {

                with(bind) {

                    // itemCount == -1 signals "loading"; keep list visible to avoid empty-state flash
                    recyclerView.isVisible = it.itemCount == -1 || it.itemCount > 0

                    // Reset all placeholders before showing the relevant one
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
