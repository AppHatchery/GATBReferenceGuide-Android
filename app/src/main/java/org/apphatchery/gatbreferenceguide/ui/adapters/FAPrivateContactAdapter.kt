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

    private var currentLabel = ""
    private var labelFlag = true
    private var firstLetter = ""

    inner class FAPrivateContactViewHolder(
        private val binding: FragmentContactItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: PrivateContact) = binding.apply {
            fullNameTextView.text = contact.fullName
            firstLetter = contact.fullName.substring(0, 1).uppercase()

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