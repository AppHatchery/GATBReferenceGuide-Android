package org.apphatchery.gatbreferenceguide.ui.adapters

import androidx.recyclerview.widget.DiffUtil

abstract class BaseDiffUtilCallBack<T> : DiffUtil.ItemCallback<T>() {
    override fun areContentsTheSame(oldItem: T, newItem: T) =
        oldItem.hashCode() == newItem.hashCode()
}