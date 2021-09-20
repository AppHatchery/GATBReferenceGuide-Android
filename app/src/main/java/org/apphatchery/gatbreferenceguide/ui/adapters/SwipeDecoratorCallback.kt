package org.apphatchery.gatbreferenceguide.ui.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import org.apphatchery.gatbreferenceguide.R


abstract class SwipeDecoratorCallback(private val context: Context) :
    ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN
                or ItemTouchHelper.START or ItemTouchHelper.END,
        ItemTouchHelper.LEFT
    ) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = true

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
            .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_forever)
            .addSwipeLeftLabel("Delete")
            .setSwipeLeftLabelColor(Color.WHITE)
            .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, R.color.reddish))
            .create()
            .decorate()
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}