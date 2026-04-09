// ListAdapter rendering the public TB-contact list in ContactFragment's RecyclerView.
// Each row uses FragmentContactItemBinding and shows the contact's full name. Contacts are
// expected to arrive pre-sorted alphabetically (by ContactDao); this adapter adds a floating
// alphabetic section label (textViewLabel) above the first contact of each letter group.
//
// Section-label logic: the adapter tracks the most-recently-seen first letter in currentLabel.
// When a new contact's initial differs, labelFlag is set true and the label becomes visible;
// otherwise it is hidden. Because this state is stored at the adapter level (not per-ViewHolder),
// fast scrolling or RecyclerView recycling can occasionally produce stale labels — acceptable
// for this use case since the list is small and fully pre-sorted.
//
// Related files:
//   - Contact (db/entities) — data class with id, fullName, phone, etc.
//   - ContactFragment — attaches this adapter; also hosts FAPrivateContactAdapter for private contacts
//   - FAContactViewPagerAdapter — wraps this adapter inside the contacts ViewPager
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
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactAdapter.FAContactViewHolder

class FAContactAdapter : ListAdapter<Contact, FAContactViewHolder>(DiffUtilCallBack()) {



    class DiffUtilCallBack : DiffUtil.ItemCallback<Contact>() {
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun itemClickCallback(listener: ((Contact) -> Unit)) {
        onItemClickListAdapter = listener
    }

    private var onItemClickListAdapter: ((Contact) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAContactViewHolder {
        return FAContactViewHolder(
            FragmentContactItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: FAContactViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    // Tracks the last-seen first letter so consecutive contacts with the same initial share one label
    private var currentLabel = ""
    private var labelFlag = true
    private var firstLetter = ""

    inner class FAContactViewHolder(
        private val binding: FragmentContactItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) = binding.apply {
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
