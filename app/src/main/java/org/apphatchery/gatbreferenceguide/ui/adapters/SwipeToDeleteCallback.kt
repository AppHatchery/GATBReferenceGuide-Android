// Abstract ItemTouchHelper callback that manually draws a delete affordance on LEFT swipe using
// the Canvas API directly — no third-party decorator library required. This is an alternative
// to SwipeDecoratorCallback for RecyclerViews where the decorator library is not wired up.
//
// Visual rendering in onChildDraw:
//   - Cancellation guard: when dX == 0 and the gesture is no longer active (isCanceled), the
//     previously drawn background is erased via clearCanvas() using PorterDuff.Mode.CLEAR, then
//     the default super draw is called so the item snaps back cleanly.
//   - Background: a ColorDrawable is stretched to fill the revealed area (itemView.right + dX to
//     itemView.right). The backgroundColor field is commented out — the host fragment is expected
//     to tint or replace the drawable externally, or the default transparent drawable is used.
//   - Icon: the trash-can icon (ic_baseline_delete_forever) is centred vertically within the row
//     and right-aligned with a margin equal to (itemHeight - iconHeight) / 2.
//
// Drag-and-drop: onMove returns true (unlike SwipeDecoratorCallback) but no drag logic is
// implemented — concrete subclasses should override if drag reordering is needed.
//
// Related files:
//   - SwipeDecoratorCallback — preferred alternative that uses the RecyclerViewSwipeDecorator
//     library for richer visuals (background colour, label, icon in one call)
//   - FASavedViewPagerAdapter — selects which callback to attach per RecyclerView page
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.R


abstract class SwipeToDeleteCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon =
        ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_forever)!!
    private val intrinsicWidth = deleteIcon.intrinsicWidth
    private val intrinsicHeight = deleteIcon.intrinsicHeight
    private val background = ColorDrawable()
//    private val backgroundColor = Color.parseColor("#f44336")

    // Used to erase the swipe background when the gesture is cancelled / item snaps back
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        // If the swipe was cancelled (finger lifted without completing), clear the drawn background
        if (isCanceled) {
            clearCanvas(
                canvas,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(
                canvas,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
            return
        }

        // Draw the red delete background
//        background.color = backgroundColor
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(canvas)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
        deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon.draw(canvas)
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = true
}
