package org.apphatchery.gatbreferenceguide.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentContactItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.Contact
import org.apphatchery.gatbreferenceguide.ui.adapters.FAContactAdapter.FAContactViewHolder

class FAContactAdapter : ListAdapter<Contact, FAContactViewHolder>(DIFF_UTIL) {


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

    private var currentLabel = ""
    private var labelFlag = true
    private var firstLetter = ""

    inner class FAContactViewHolder(
        private val binding: FragmentContactItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) = binding.apply {
            fullNameTextView.text = contact.firstName
            firstLetter = contact.firstName.substring(0, 1).uppercase()

            labelFlag = if (firstLetter != currentLabel) {
                currentLabel = firstLetter
                true
            } else false

            textViewLabel.text = firstLetter
            textViewLabel.isVisible = labelFlag

        }
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<Contact>() {
            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}