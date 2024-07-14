package org.apphatchery.gatbreferenceguide.ui.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

abstract class BaseDiffUtilCallBack<T> : DiffUtil.ItemCallback<T>() {
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any) =
        oldItem.hashCode() == newItem.hashCode()
}