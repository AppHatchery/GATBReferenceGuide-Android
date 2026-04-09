// ListAdapter rendering the sub-chapter list for a selected chapter in SubChapterFragment's
// RecyclerView. Each row uses the shared FragmentWithRecyclerviewItemBinding layout and displays
// an alphabetic prefix followed by the sub-chapter title, e.g. "A. Overview of TB Treatment".
//
// Alphabetic prefixing: the adapter position is used as a direct index into the ALPHABET constant
// array (["a","b","c",...]), converted to uppercase. This assumes sub-chapters arrive in the
// correct display order from SubChapterDao (ordered by subChapterId within the parent chapter).
// If the number of sub-chapters ever exceeds the length of ALPHABET an IndexOutOfBoundsException
// would occur — in practice TB guide chapters have far fewer sub-chapters than 26.
//
// Related files:
//   - SubChapterEntity (db/entities) — holds subChapterId, subChapterTitle, and parent chapterId
//   - ALPHABET (utils) — lowercase letter array used for the A/B/C prefix
//   - SubChapterFragment — attaches this adapter and handles itemClickCallback to open BodyFragment
//   - FAChapterAdapter — the parent-level adapter; selecting a chapter leads to this sub-chapter list
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.utils.ALPHABET

class FASubChapterAdapter :
    ListAdapter<SubChapterEntity, FASubChapterAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<SubChapterEntity>() {
        override fun areItemsTheSame(oldItem: SubChapterEntity, newItem: SubChapterEntity) =
            newItem.subChapterId == oldItem.subChapterId

        override fun areContentsTheSame(oldItem: SubChapterEntity, newItem: SubChapterEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((SubChapterEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }

    var onItemClickListAdapter: ((SubChapterEntity) -> Unit)? = null


    inner class ViewHolder(private val bind: FragmentWithRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun onBinding(chapterEntity: SubChapterEntity, index: Int) =
            bind.apply {
                // Prefix each sub-chapter with its alphabetic letter: "A. ", "B. ", etc.
                (ALPHABET[index].uppercase() + ". " + chapterEntity.subChapterTitle).also {
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
