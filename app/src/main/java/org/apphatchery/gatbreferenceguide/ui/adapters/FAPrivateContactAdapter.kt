// ListAdapter rendering the private/personal TB-contact list in ContactFragment's second ViewPager
// tab. Structurally mirrors FAContactAdapter but operates on PrivateContact entities instead of
// the shared Contact entities, keeping personal contacts separate from the read-only public list.
//
// Section-label logic is identical to FAContactAdapter: the adapter tracks currentLabel (the
// last-seen first letter) and shows textViewLabel only when the initial changes between
// consecutive items, producing alphabetic group headers. Contacts must be pre-sorted
// alphabetically by the caller (PrivateContactDao or the host fragment) for headers to be correct.
//
// Reuses FragmentContactItemBinding — the same row layout as FAContactAdapter — so both tabs
// look visually consistent.
//
// Related files:
//   - PrivateContact (db/entities) — data class with id, fullName, phone, and other private fields
//   - FAContactAdapter — the public-contacts counterpart; shares the same layout and label logic
//   - ContactFragment — attaches both adapters via FAContactViewPagerAdapter
//   - SwipeToDeleteCallback / SwipeDecoratorCallback — wired to this adapter's RecyclerView in
//     ContactFragment to allow left-swipe deletion of private contacts
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.db.entities.PrivateContact
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactAdapter.FAContactViewHolder

class FAPrivateContactAdapter : ListAdapter<PrivateContact, FAPrivateContactAdapter.FAPrivateContactViewHolder>(DiffUtilCallBack()) {

    class DiffUtilCallBack : DiffUtil.ItemCallback<PrivateContact>() {
        override fun areContentsTheSame(oldItem: PrivateContact, newItem: PrivateContact): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: PrivateContact, newItem: PrivateContact): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun itemClickCallback(listener: ((PrivateContact) -> Unit)) {
        onItemClickListAdapter = listener
    }

    private var onItemClickListAdapter: ((PrivateContact) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAPrivateContactViewHolder {
        return FAPrivateContactViewHolder(
            FragmentContactItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: FAPrivateContactViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    // Tracks the last-seen first letter so consecutive contacts with the same initial share one label
    private var currentLabel = ""
    private var labelFlag = true
    private var firstLetter = ""

    inner class FAPrivateContactViewHolder(
        private val binding: FragmentContactItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: PrivateContact) = binding.apply {
            fullNameTextView.text = contact.fullName
            firstLetter = contact.fullName.substring(0, 1).uppercase()

            // Show the letter header only when the initial changes from the previous contact
            labelFlag = if (firstLetter != currentLabel) {
                currentLabel = firstLetter
                true
            } else false

            textViewLabel.text = firstLetter
            textViewLabel.isVisible = labelFlag

        }

        init {
            binding.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }

        }

    }
}
