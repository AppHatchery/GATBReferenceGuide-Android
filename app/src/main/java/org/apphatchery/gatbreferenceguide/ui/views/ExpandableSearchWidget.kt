// Compound view that provides in-page search for the TB guide's article WebView.
// Inflates R.layout.search_view and manages its own expand/collapse animation, match
// navigation, and content highlighting.
//
// Usage in BodyFragment:
//   1. Call setContentToSearch(htmlString) after the WebView page finishes loading.
//   2. Attach onMatchNavigated to receive (currentMatch, totalMatches) for the counter label.
//   3. Call getHighlightedContent() to get the HTML with <span> highlights injected, then
//      load it back into the WebView.
//   4. Call hideSearchView() / showSearchView() to toggle visibility from the toolbar.
//
// Expand animation: the EditText animates from 0 width to 213 dp over 250 ms. The 213 dp
// value is a fixed design constant leaving room for the prev/next/clear icons on the right.
//
// Highlight injection: uses Jsoup to strip tags for match-counting, then wraps matched words
// in <span style='background-color:yellow'> in the original HTML for re-loading.
// Related: BaseWebView.kt (hosts the loaded content), R.layout.search_view,
//          R.string.search_counter_placeholder, BodyFragment.
package org.apphatchery.gatbreferenceguide.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import org.apphatchery.gatbreferenceguide.R
import org.jsoup.Jsoup
import java.util.regex.Pattern

class ExpandableSearchWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val searchEditText: EditText
    private val searchCounter: TextView
    private val searchPrevious: ImageView
    private val searchNext: ImageView
    private val searchClear: ImageView

    private var isExpanded = false
    private var currentMatch = 0
    private var totalMatches = 0
    private var searchMatches = mutableListOf<String>()
    private var originalContent = ""
    private var highlightedContent = ""

    // Callbacks
    var onSearchStarted: (() -> Unit)? = null
    var onSearchCleared: (() -> Unit)? = null
    var onSearchTextChanged: ((String) -> Unit)? = null
    var onMatchNavigated: ((Int, Int) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.search_view, this, true)

        searchEditText = findViewById(R.id.search_edit_text)
        searchCounter = findViewById(R.id.search_counter)
        searchPrevious = findViewById(R.id.search_previous)
        searchNext = findViewById(R.id.search_next)
        searchClear = findViewById(R.id.search_clear)

        setupListeners()
    }

    private fun setupListeners() {
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !isExpanded) {
                expandSearchView()
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                onSearchTextChanged?.invoke(query)
                
                if (query.isNotEmpty()) {
                    searchClear.visibility = View.VISIBLE
                    if (isExpanded) {
                        performSearch(query)
                    }
                } else {
                    searchClear.visibility = View.GONE
                    clearSearch()
                }
            }
        })

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    if (!isExpanded) {
                        expandSearchView()
                    }
                    performSearch(query)
                }
                true
            } else false
        }

        searchClear.setOnClickListener {
            searchEditText.text?.clear()
            clearSearch()
            onSearchCleared?.invoke()
        }

        searchPrevious.setOnClickListener { navigatePrevious() }
        searchNext.setOnClickListener { navigateNext() }
    }

    private fun expandSearchView() {
        if (isExpanded) return
        isExpanded = true
        onSearchStarted?.invoke()

        // Animate EditText width to 213dp
        val targetWidth = (213 * resources.displayMetrics.density).toInt()

        ValueAnimator.ofInt(searchEditText.width, targetWidth).apply {
            duration = 250
            addUpdateListener { animation ->
                val layoutParams = searchEditText.layoutParams
                layoutParams.width = animation.animatedValue as Int
                searchEditText.layoutParams = layoutParams
            }
            doOnEnd {
                // Change background and show controls if there's text
                searchEditText.background = ContextCompat.getDrawable(context, R.drawable.search_input_background)
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchClear.visibility = View.VISIBLE
                    performSearch(query)
                }
            }
        }.start()
    }

    /**
     * Shrinks the EditText back to full width, hides counter/nav controls, and clears results.
     * Called by BodyFragment when the user closes the search toolbar or navigates away.
     * Safe to call even when the widget is already collapsed (returns early).
     */
    fun collapseSearchView() {
        if (!isExpanded) return
        isExpanded = false

        // Hide controls first
        searchCounter.visibility = View.GONE
        searchPrevious.visibility = View.GONE
        searchNext.visibility = View.GONE
        searchClear.visibility = View.GONE

        // Animate back to full width
        val targetWidth = LayoutParams.MATCH_PARENT
        val parentWidth = (parent as? View)?.width ?: resources.displayMetrics.widthPixels
        val actualTargetWidth = parentWidth - paddingStart - paddingEnd

        ValueAnimator.ofInt(searchEditText.width, actualTargetWidth).apply {
            duration = 250
            addUpdateListener { animation ->
                val layoutParams = searchEditText.layoutParams
                layoutParams.width = animation.animatedValue as Int
                searchEditText.layoutParams = layoutParams
            }
            doOnEnd {
                val layoutParams = searchEditText.layoutParams
                layoutParams.width = LayoutParams.MATCH_PARENT
                searchEditText.layoutParams = layoutParams
                searchEditText.background = ContextCompat.getDrawable(context, R.drawable.search_input_background)
            }
        }.start()

        searchEditText.clearFocus()
        clearSearch()
    }

    /**
     * Counts case-insensitive matches of [query] in the plain text extracted from
     * [originalContent] via Jsoup, updates the counter, and triggers highlight injection.
     * Must be called after [setContentToSearch] has supplied the HTML source.
     */
    private fun performSearch(query: String) {
        if (query.isEmpty() || originalContent.isEmpty()) {
            clearSearchResults()
            return
        }

        // Find all matches in the content
        searchMatches.clear()
        val pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(Jsoup.parse(originalContent).text())
        
        while (matcher.find()) {
            searchMatches.add(matcher.group())
        }

        totalMatches = searchMatches.size
        currentMatch = if (totalMatches > 0) 1 else 0

        updateSearchUI()
        highlightCurrentMatch(query)
    }

    private fun highlightCurrentMatch(query: String) {
        if (query.isEmpty() || originalContent.isEmpty()) return

        // Create highlighted version using similar logic to GlobalSearchFragment
        val searchWords = query.split(Regex("[\\s.,]+")).filter { it.isNotEmpty() }
        highlightedContent = highlightText(originalContent, searchWords)
        
        onMatchNavigated?.invoke(currentMatch, totalMatches)
    }

    private fun highlightText(original: String, wordsToHighlight: List<String>): String {
        if (wordsToHighlight.isEmpty() || original.isEmpty()) return original
        var result = original
        for (word in wordsToHighlight) {
            if (word.isNotEmpty()) {
                val pattern = word.replace(Regex("[\\s.,]+"), "")
                if (pattern.isNotEmpty()) {
                    val regex = Regex("(?i)($pattern)")
                    result = result.replace(regex) {
                        "<span style='background-color: yellow; color: black; font-weight: bold;'>${it.value}</span>"
                    }
                }
            }
        }
        return result
    }

    private fun updateSearchUI() {
        if (totalMatches > 0) {
            searchCounter.text = context.getString(R.string.search_counter_placeholder, currentMatch, totalMatches)
            searchCounter.visibility = View.VISIBLE
            searchPrevious.visibility = View.VISIBLE
            searchNext.visibility = View.VISIBLE
        } else {
            searchCounter.text = "0/0"
            searchCounter.visibility = View.VISIBLE
            searchPrevious.visibility = View.VISIBLE
            searchNext.visibility = View.VISIBLE
        }
    }

    private fun navigatePrevious() {
        if (currentMatch > 1) {
            currentMatch--
            updateSearchCounter()
            onMatchNavigated?.invoke(currentMatch, totalMatches)
        }
    }

    private fun navigateNext() {
        if (currentMatch < totalMatches) {
            currentMatch++
            updateSearchCounter()
            onMatchNavigated?.invoke(currentMatch, totalMatches)
        }
    }

    private fun updateSearchCounter() {
        if (totalMatches > 0) {
            searchCounter.text = context.getString(R.string.search_counter_placeholder, currentMatch, totalMatches)
        } else {
            searchCounter.text = "0/0"
        }
    }

    private fun clearSearch() {
        clearSearchResults()
        if (isExpanded) {
            searchCounter.visibility = View.GONE
            searchPrevious.visibility = View.GONE
            searchNext.visibility = View.GONE
        }
    }

    private fun clearSearchResults() {
        currentMatch = 0
        totalMatches = 0
        searchMatches.clear()
        highlightedContent = ""
    }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    // Public methods for external usage
    fun setContentToSearch(content: String) {
        originalContent = content
    }

    fun getSearchQuery(): String = searchEditText.text.toString().trim()

    fun getHighlightedContent(): String = highlightedContent.ifEmpty { originalContent }

    fun getCurrentMatch(): Int = currentMatch
    fun getTotalMatches(): Int = totalMatches

    fun showSearchView() {
        visibility = View.VISIBLE
    }

    fun hideSearchView() {
        visibility = View.GONE
        collapseSearchView()
    }

    fun clearSearchText() {
        searchEditText.text?.clear()
    }
}
