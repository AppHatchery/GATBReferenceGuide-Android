package org.apphatchery.gatbreferenceguide.db.data

import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeDecoratorCallback
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeToDeleteCallback

data class ViewPagerData(
    val recyclerViewAdapter: RecyclerView.Adapter<*>,
    val swipeToDeleteCallback: Any? = null
)