// ListAdapter rendering the colour picker grid in the "Add / Edit Note" dialog.
// Items are NoteColor objects (each wrapping a hex colour string from the NOTE_COLOR constant
// list). Each cell displays a coloured circle; the currently selected colour shows a distinct
// "selected" ring (selectedTag + selectedColorInner views visible), while all other cells show
// only the unselected ring (unselectedTag visible).
//
// Selection state is managed entirely within the adapter via selectedColor (defaults to the first
// NOTE_COLOR entry). When the user taps a cell, selectedColor is updated and notifyDataSetChanged
// is called so every cell re-evaluates its selected/unselected visibility. The host dialog reads
// selectedColor directly after the user confirms their choice.
//
// Color tinting: rather than inflating separate layouts per colour, both the selected and
// unselected drawables have their tint set programmatically via background.setTint(color).
//
// Related files:
//   - NoteColor (db/data) — simple data class wrapping a hex color string
//   - NOTE_COLOR (utils) — pre-defined list of available note colours
//   - FANoteAdapter — renders notes using the hex color string chosen here
//   - Note add/edit dialog fragment — hosts this adapter and reads selectedColor on confirm
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.DialogNoteColorItemBinding
import org.apphatchery.gatbreferenceguide.db.data.NoteColor
import org.apphatchery.gatbreferenceguide.utils.NOTE_COLOR

class FANoteColorAdapter(private val context: Context) :
    ListAdapter<NoteColor, FANoteColorAdapter.ViewHolder>(DiffUtilCallBack()) {


    /** The hex color string of the currently selected swatch; read by the host dialog on confirm. */
    var selectedColor: String = NOTE_COLOR[0].color

    class DiffUtilCallBack : DiffUtil.ItemCallback<NoteColor>() {
        override fun areItemsTheSame(oldItem: NoteColor, newItem: NoteColor) =
            newItem.color == oldItem.color

        override fun areContentsTheSame(oldItem: NoteColor, newItem: NoteColor) =
            oldItem == newItem
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(private val dialogNoteColorItemBinding: DialogNoteColorItemBinding) :
        RecyclerView.ViewHolder(dialogNoteColorItemBinding.root) {

        fun onBinding(noteColor: NoteColor) =
            dialogNoteColorItemBinding.apply {
                val color = Color.parseColor(noteColor.color)

                if (selectedColor == noteColor.color) {
                    // Show selected state
                    unselectedTag.visibility = android.view.View.GONE
                    selectedTag.visibility = android.view.View.VISIBLE
                    selectedColorInner.visibility = android.view.View.VISIBLE
                    selectedColorInner.background.setTint(color)
                } else {
                    // Show unselected state
                    unselectedTag.visibility = android.view.View.VISIBLE
                    selectedTag.visibility = android.view.View.GONE
                    selectedColorInner.visibility = android.view.View.GONE
                    unselectedTag.background.setTint(color)
                }
            }

        init {
            // On tap: update selection and refresh all cells so the ring indicator moves
            dialogNoteColorItemBinding.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    selectedColor = currentClickedItem.color
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DialogNoteColorItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position))
    }
}
