// Generic base class for DiffUtil.ItemCallback used across multiple adapters in this project.
//
// Pattern: adapters that need a DiffCallback extend BaseDiffUtilCallBack<T> and only override
// areItemsTheSame() (identity check, usually by entity ID). areContentsTheSame() is provided
// here using hashCode() equality, which works for any data class since Kotlin data classes
// generate hashCode() from all constructor properties. This avoids duplicating the same
// areContentsTheSame() boilerplate in every adapter.
//
// @SuppressLint("DiffUtilEquals"): DiffUtil lint warns when areContentsTheSame() does not use
// structural equals(). The hashCode()-based comparison is intentional here — it correctly detects
// any field change in Kotlin data classes while remaining safe for the non-null bound T & Any.
//
// Adopters: FAChapterAdapter, FAChartAdapter, FAMainFirst6ChartAdapter, and others that prefer
// hashCode comparison. Adapters needing stricter equality (e.g. FAContactAdapter) define their
// own inline DiffUtilCallBack instead.
package org.apphatchery.gatbreferenceguide.ui.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

abstract class BaseDiffUtilCallBack<T> : DiffUtil.ItemCallback<T>() {
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any) =
        oldItem.hashCode() == newItem.hashCode()
}
