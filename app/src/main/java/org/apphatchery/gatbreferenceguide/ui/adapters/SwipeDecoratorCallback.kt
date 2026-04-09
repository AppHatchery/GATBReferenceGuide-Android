// Abstract ItemTouchHelper callback that draws a styled delete affordance when the user swipes
// a RecyclerView row to the LEFT. Used on the Saved screen (SavedFragment) for the Bookmarks
// and Recents tabs, and on the Contacts screen for private contacts.
//
// Visual decoration (onChildDraw): delegates to the RecyclerViewSwipeDecorator library to paint:
//   - A red background (R.color.reddish) behind the swiped item.
//   - A white "Delete" label.
//   - A trash-can icon (ic_baseline_delete_forever) aligned to the right.
//
// Drag-and-drop is disabled (onMove returns false). Only LEFT swipe is enabled
// (ItemTouchHelper.LEFT passed to SimpleCallback).
//
// Concrete subclasses must implement onSwiped() to handle the actual deletion (e.g. calling
// the ViewModel to delete the item from the Room database).
//
// Related files:
//   - SwipeToDeleteCallback — alternative manual implementation without the decorator library;
//     draws the background and icon directly via Canvas (used where the library is unavailable)
//   - FASavedViewPagerAdapter — attaches concrete subclass instances to each tab's RecyclerView
//     via ItemTouchHelper, using the swipeToDeleteCallback field of ViewPagerData
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
        0, ItemTouchHelper.LEFT
    ) {

    // Drag reordering is not supported in this app; always return false
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

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
