package org.apphatchery.gatbreferenceguide.ui.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.db.data.RecentSearchItem

class FASearchRecentAdapter(
    private val onItemClicked: (String) -> Unit
) : RecyclerView.Adapter<FASearchRecentAdapter.ViewHolder>(){

    private val recentSearches = mutableListOf<RecentSearchItem>()
    private lateinit var sharedPreferences: SharedPreferences

    constructor(context: Context, onItemClicked: (String) -> Unit): this(onItemClicked){
        sharedPreferences = context.getSharedPreferences("RECENT_SEARCHES", Context.MODE_PRIVATE)
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        val recentSearchesString = sharedPreferences.getString("RECENT_SEARCHES_LIST", null)
        recentSearches.clear()
        recentSearches.addAll(recentSearchesString?.split(",")?.map { RecentSearchItem(it) } ?: emptyList())
    }

    fun updateRecentSearches(newSearch : String){

        if (newSearch.trim().isNotEmpty()) {
            val item = RecentSearchItem(newSearch)
            if (!recentSearches.contains(item)) {
                recentSearches.add(0, item)
                if (recentSearches.size > 3) {
                    recentSearches.removeLast()
                }
                saveRecentSearches()
                notifyDataSetChanged()
            }
        }
    }

    private fun saveRecentSearches() {
        val recentSearchesString = recentSearches.joinToString(",") { it.searchText }
        sharedPreferences.edit().putString("RECENT_SEARCHES_LIST", recentSearchesString).apply()
    }


    override fun getItemCount(): Int = recentSearches.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textview_recent_title)

        fun bind(item: RecentSearchItem) {
            textView.text = item.searchText

        }
        init {
            itemView.setOnClickListener {
                val searchText = recentSearches[adapterPosition].searchText
                onItemClicked(searchText)
            }

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FASearchRecentAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_search_recent_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FASearchRecentAdapter.ViewHolder, position: Int) {
        holder.bind(recentSearches[position])
    }

}