package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.R

class ShareTargetsAdapter(
    private val targets: List<ResolveInfo>,
    private val onItemClick: (ResolveInfo) -> Unit
) : RecyclerView.Adapter<ShareTargetsAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val label: TextView = itemView.findViewById(R.id.app_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_share_target, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val info = targets[position]
        val pm = holder.itemView.context.packageManager
        holder.icon.setImageDrawable(info.loadIcon(pm))
        holder.label.text = info.loadLabel(pm)
        holder.itemView.setOnClickListener { onItemClick(info) }
    }

    override fun getItemCount(): Int = targets.size
}


