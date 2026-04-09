// ListAdapter rendering user notes in two different contexts:
//   1. NoteFragment (Saved screen) — shows all notes with a "Note - Last edited <date>" header.
//   2. BodyFragment (in-chapter view) — shows only notes for the current sub-chapter, displaying
//      the note title instead of the last-edited date.
//
// The dual-mode behaviour is controlled by the viewInChapter constructor parameter (default
// View.GONE). When the host passes View.VISIBLE, textviewNoteTitle shows note.noteTitle and
// textviewShowChapter becomes visible; otherwise it shows the formatted last-edit timestamp.
//
// Color coding: each note has a noteColor hex string (e.g. "#FF5733"). noteLeftBar is a thin
// vertical stripe whose background is set to Color.parseColor(note.noteColor), giving each note
// a colour-coded left border matching the colour chosen at creation time.
//
// Tap target: the edit icon (noteEditIcon) fires itemClickCallback — tapping the card body
// itself does NOT trigger navigation; only the pencil icon opens the edit dialog.
//
// Related files:
//   - NoteEntity (db/entities) — holds noteId, noteText, noteTitle, noteColor, lastEditDateFormat
//   - NoteFragment / BodyFragment — attach this adapter; BodyFragment passes View.VISIBLE
//   - FANoteColorAdapter — sibling adapter for the colour picker in the note creation dialog
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.databinding.FragmentNoteItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity

class FANoteAdapter(val viewInChapter: Int = View.GONE) :
    ListAdapter<NoteEntity, FANoteAdapter.ViewHolder>(DiffUtilCallBack()) {


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
                // Left colour stripe uses the hex string stored with the note
                noteLeftBar.setBackgroundColor(Color.parseColor(note.noteColor))
                textviewNoteBody.text = note.noteText
                // In-chapter context shows the note title; saved-notes list shows last-edit date
                ("Note - Last edited " + note.lastEditDateFormat).also {
                    textviewNoteTitle.text =
                        if (viewInChapter == View.VISIBLE) note.noteTitle else it
                }
            }

        init {
            fragmentNoteItemBinding.apply {
                textviewShowChapter.visibility = viewInChapter
                noteEditIcon.setOnClickListener {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentNoteItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position))
    }
}
