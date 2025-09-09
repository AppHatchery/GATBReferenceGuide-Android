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

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).bookmarkId.contains("table_")) {
            TYPE_CHART
        } else {
            TYPE_CHAPTER
        }
    }

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