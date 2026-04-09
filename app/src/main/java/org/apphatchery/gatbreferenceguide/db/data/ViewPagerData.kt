// Bundle passed to the ViewPager2 setup helper that configures a single tab's RecyclerView inside
// a tabbed screen (e.g. the Saved screen's Bookmarks / Notes / Recents tabs).
//
// Fields:
//   recyclerViewAdapter    — the RecyclerView.Adapter instance for this tab's list; typed as
//                            Adapter<*> so the same helper can accept different item types across
//                            tabs without casting at the call site.
//   swipeToDeleteCallback  — optional ItemTouchHelper callback enabling swipe-to-delete on list
//                            rows. Typed as Any? because tabs that don't support swipe (e.g. a
//                            read-only list) pass null; the helper checks for null before attaching.
//                            Expected runtime types: SwipeToDeleteCallback or SwipeDecoratorCallback.
//
// Related: SwipeDecoratorCallback, SwipeToDeleteCallback, FASavedFragment, ViewPager2 tab setup.
package org.apphatchery.gatbreferenceguide.db.data

import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeDecoratorCallback
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeToDeleteCallback

data class ViewPagerData(
    val recyclerViewAdapter: RecyclerView.Adapter<*>,
    val swipeToDeleteCallback: Any? = null
)
