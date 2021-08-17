package org.apphatchery.gatbreferenceguide.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentNoteItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity

class FABodyNoteAdapter :
    ListAdapter<NoteEntity, FABodyNoteAdapter.ViewHolder>(DiffUtilCallBack()) {


    class DiffUtilCallBack : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            newItem.noteId == oldItem.noteId

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((NoteEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }

    private var onItemClickListAdapter: ((NoteEntity) -> Unit)? = null


    inner class ViewHolder(private val fragmentNoteItemBinding: FragmentNoteItemBinding) :
        RecyclerView.ViewHolder(fragmentNoteItemBinding.root) {

        fun onBinding(note: NoteEntity) =
            fragmentNoteItemBinding.apply {
                view.setBackgroundColor(Color.parseColor(note.noteColor))
                textviewNoteBody.text = note.noteText
                ("Note - Last edited " + note.lastEditDateFormat).also { textviewNoteTitle.text = it }
            }

        init {
            fragmentNoteItemBinding.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentNoteItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position))
    }
}