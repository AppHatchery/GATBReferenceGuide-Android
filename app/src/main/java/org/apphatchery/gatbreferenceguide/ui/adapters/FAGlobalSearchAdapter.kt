package org.apphatchery.gatbreferenceguide.ui.adapters

import android.graphics.Rect
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.apphatchery.gatbreferenceguide.databinding.FragmentGlobalSearchItemBinding
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAGlobalSearchViewModel
import javax.inject.Inject

class FAGlobalSearchAdapter @Inject constructor(
) : ListAdapter<GlobalSearchEntity, FAGlobalSearchAdapter.ViewHolder>(DiffUtilCallBack()) {

    var searchQuery: String = ""
    var searchQuery_ = MutableStateFlow("")



    class DiffUtilCallBack : DiffUtil.ItemCallback<GlobalSearchEntity>() {
        override fun areItemsTheSame(oldItem: GlobalSearchEntity, newItem: GlobalSearchEntity) =
            newItem.fileName == oldItem.fileName

        override fun areContentsTheSame(oldItem: GlobalSearchEntity, newItem: GlobalSearchEntity) =
            oldItem == newItem
    }

    fun itemClickCallback(listener: ((GlobalSearchEntity) -> Unit)) {
        onItemClickListAdapter = listener
    }


    private var onItemClickListAdapter: ((GlobalSearchEntity) -> Unit)? = null

    private fun prepSearchQuery(textInBody: String) =
        textInBody.subSequence(getSearchStartPosition(textInBody), textInBody.length).toString()

    private fun getSearchStartPosition(textInBody: String) =
        if (textInBody.lowercase().indexOf(searchQuery) == -1) 0
        else textInBody.lowercase().indexOf(searchQuery)

    inner class ViewHolder(private val fragmentGlobalSearchItemBinding: FragmentGlobalSearchItemBinding) :
        RecyclerView.ViewHolder(fragmentGlobalSearchItemBinding.root) {

        fun onBinding(globalSearchEntity: GlobalSearchEntity, index: Int) =
            fragmentGlobalSearchItemBinding.apply {

                searchTitle.text = HtmlCompat.fromHtml(globalSearchEntity.searchTitle,FROM_HTML_MODE_LEGACY)
                subChapter.text = HtmlCompat.fromHtml(globalSearchEntity.subChapter,FROM_HTML_MODE_LEGACY)
                textInBody.text = HtmlCompat.fromHtml(globalSearchEntity.textInBody, FROM_HTML_MODE_LEGACY)

                //  val term = globalSearchEntity.searched
                val bodyWithTags = globalSearchEntity.textInBody
                val pattern = ".*<span style='background-color: yellow; color: black; font-weight: bold;'>(.*?)</span>.*".toRegex()
                val matchResult = pattern.find(bodyWithTags)
                val extractedSearchValue = matchResult?.groupValues?.get(1) ?: ""

                val locationOfTarget =  textInBody.text.indexOf(extractedSearchValue)

               // Log.d("mmmmmmmmm",extractedSearchValue+".."+searchQuery_.value)

//                if (locationOfTarget != -1) {
//                    textInBody.maxLines = 2
//                    textInBody.ellipsize = TextUtils.TruncateAt.MARQUEE
//                    textInBody.post {
//                        val line = textInBody.layout.getLineForOffset(locationOfTarget)
//                        val y = textInBody.layout.getLineTop(line)
//                        textInBody.scrollTo(0, y)
//                    }
//                }
                // Check if the target location is within the textInBody view
                if (locationOfTarget != -1) {
                    textInBody.maxLines = 3
                    textInBody.ellipsize = TextUtils.TruncateAt.START

                    textInBody.post {
                        val line = textInBody.layout.getLineForOffset(locationOfTarget)
                        val y = textInBody.layout.getLineTop(line)
                        val x = textInBody.layout.getPrimaryHorizontal(locationOfTarget)

                        // Calculate visible dimensions (excluding padding) of the textInBody view
                        val visibleWidth = textInBody.width - textInBody.paddingStart - textInBody.paddingEnd
                        val visibleHeight = textInBody.height - textInBody.paddingTop - textInBody.paddingBottom

                        // Get the bounds of the line containing the target location
                        val targetRect = Rect()
                        textInBody.layout.getLineBounds(line, targetRect)

                        // Calculate the width of the target line
                        val targetWidth = targetRect.width()

                        // Scroll to the new position to make the target visible
                        var newX = x.toInt()
                        var newY = y

                        // Loop until the target is fully visible
                        while (newX < textInBody.scrollX || newX + targetWidth > textInBody.scrollX + visibleWidth ||
                            newY < textInBody.scrollY || newY + targetRect.height() > textInBody.scrollY + visibleHeight) {
                            if (newX < textInBody.scrollX) {
                                newX += 10 // Scroll right by 10 pixels (adjust as needed)
                            } else if (newX + targetWidth > textInBody.scrollX + visibleWidth) {
                                newX -= 10 // Scroll left by 10 pixels (adjust as needed)
                            }

                            if (newY < textInBody.scrollY) {
                                newY += 30 // Scroll down by 10 pixels (adjust as needed)
                            } else if (newY + targetRect.height() > textInBody.scrollY + visibleHeight) {
                                newY -= 30 // Scroll up by 10 pixels (adjust as needed)
                            }

                            textInBody.scrollTo(newX, newY)
                        }
                    }
                }
            }

        init {
            fragmentGlobalSearchItemBinding.root.setOnClickListener {
                if (RecyclerView.NO_POSITION != adapterPosition) {
                    val currentClickedItem = currentList[adapterPosition]
                    onItemClickListAdapter?.let {
                        it(currentClickedItem)
                    }
                }
            }

        }

    }

    private fun setSpannableString(
        spannableString: String,
        spannableTextCallback: SpannableString.() -> Unit
    ) = SpannableString(spannableString).apply {
        try {
            setSpan(
                if (spannableString.contains(searchQuery, true))
                    StyleSpan(Typeface.BOLD) else StyleSpan(Typeface.NORMAL),
                0,
                searchQuery.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTextCallback(this.trim() as SpannableString)
        } catch (e: Exception) {
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentGlobalSearchItemBinding.inflate(
            LayoutInflater.from(parent.context)
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBinding(getItem(position), position)
    }


}