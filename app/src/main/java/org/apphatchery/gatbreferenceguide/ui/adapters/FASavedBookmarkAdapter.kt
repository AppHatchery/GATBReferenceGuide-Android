// ListAdapter rendering saved bookmarks in the Bookmarks tab of SavedFragment's RecyclerView.
// Supports two distinct view types determined by the bookmarkId prefix:
//   - TYPE_CHART  (bookmarkId starts with "table_"): uses FragmentChartBookmarkItemBinding,
//     showing chartTitle and chartSource (from subChapter field).
//   - TYPE_CHAPTER (all other IDs): uses FragmentBookmarkItemBinding, showing bookmarkTitle
//     and chapterSource (also from subChapter).
//
// Both view types expose two tap targets:
//   - Root card (itemClickCallback) — navigates to the bookmarked content in BodyFragment.
//   - Edit icon (itemEditCallback, bookmarkEditIcon / chartEditIcon) — opens the rename/delete
//     dialog for that bookmark.
//
// Left-swipe deletion is handled externally via SwipeDecoratorCallback attached to the host
// RecyclerView in FASavedViewPagerAdapter; this adapter itself has no swipe logic.
//
// Related files:
//   - BookmarkEntity (db/entities) — holds bookmarkId, bookmarkTitle, subChapter (display source)
//   - SavedFragment — hosts the ViewPager; attaches swipe callback via FASavedViewPagerAdapter
//   - FASavedViewPagerAdapter — wraps this adapter in a ViewPagerData and attaches ItemTouchHelper
//   - SwipeDecoratorCallback — draws the red delete background on left-swipe
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentBookmarkItemBinding
import org.apphatchery.gatbreferenceguide.databinding.FragmentChartBookmarkItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity

class FASavedBookmarkAdapter :
    ListAdapter<BookmarkEntity, RecyclerView.ViewHolder>(DiffUtilCallBack()) {

    companion object {
        private const val TYPE_CHAPTER = 0
        private const val TYPE_CHART = 1
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<BookmarkEntity>() {
        override fun areItemsTheSame(oldItem: BookmarkEntity, newItem: BookmarkEntity) =
            newItem.bookmarkId == oldItem.bookmarkId

        override fun areContentsTheSame(oldItem: BookmarkEntity, newItem: BookmarkEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((BookmarkEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }

    fun itemEditCallback(listener: ((BookmarkEntity) -> Unit)) {
        onItemEditListAdapter = listener
    }

    private var onItemClickListAdapter: ((BookmarkEntity) -> Unit)? = null
    private var onItemEditListAdapter: ((BookmarkEntity) -> Unit)? = null

    // Bookmarks whose ID starts with "table_" were created from chart/table pages; all others
    // are chapter bookmarks. This prefix convention is set at bookmark creation time in BodyFragment.
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).bookmarkId.startsWith("table_")) {
            TYPE_CHART
        } else {
            TYPE_CHAPTER
        }
    }

    /** ViewHolder for chapter bookmarks (FragmentBookmarkItemBinding). */
    inner class ChapterViewHolder(private val bind: FragmentBookmarkItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(data: BookmarkEntity, index: Int) =
            bind.apply {
                textviewBookmarkTitle.text = data.bookmarkTitle
                textviewBookmarkName.text = data.bookmarkTitle  // Bookmark name with chapter icon
                textviewChapterSource.text = data.subChapter     // Chapter source from subChapter field
            }

        init {
            // Card click - navigate to content
            bind.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }

            // Edit icon click - show edit dialog
            bind.bookmarkEditIcon.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemEditListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }
        }
    }

    /** ViewHolder for chart/table bookmarks (FragmentChartBookmarkItemBinding). */
    inner class ChartViewHolder(private val bind: FragmentChartBookmarkItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(data: BookmarkEntity, index: Int) =
            bind.apply {
                textviewChartTitle.text = data.bookmarkTitle
                textviewChartSource.text = data.subChapter
            }

        init {
            // Card click - navigate to content
            bind.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }

            // Edit icon click - show edit dialog
            bind.chartEditIcon.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemEditListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CHART -> ChartViewHolder(
                FragmentChartBookmarkItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ChapterViewHolder(
                FragmentBookmarkItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChapterViewHolder -> holder.onBinding(getItem(position), position)
            is ChartViewHolder -> holder.onBinding(getItem(position), position)
        }
    }
}
