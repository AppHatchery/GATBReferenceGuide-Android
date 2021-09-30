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
                colorSchema.background.setTint(Color.parseColor(noteColor.color))
                if (selectedColor == noteColor.color) {
                    dialogNoteColorItemBinding.root.background =
                        ContextCompat.getDrawable(context, R.drawable.shape_circle_border)
                } else {
                    dialogNoteColorItemBinding.root.background = null
                }
            }

        init {
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