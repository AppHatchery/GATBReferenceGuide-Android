// Kotlin extension functions and helpers that reduce boilerplate in UI fragments.
//
// SearchView.onQueryTextChange   — wraps the verbose OnQueryTextListener; used in search screens.
// OnTouchHelper (abstract class) — left-swipe-to-delete helper for RecyclerViews; used in
//                                  SavedFragment (bookmarks) and NotesFragment.
// EditText.setOnTextWatcher      — fires only on non-blank changes; skips before/after callbacks.
// LiveData.observeOnce           — observes a single emission then auto-removes; avoids leaks when
//                                  a one-shot DB lookup is needed inside a fragment.
// EditText.onSearchKeyword       — pre-fills a search EditText and positions the cursor at the end.
// EditText.toggleSoftKeyboard    — shows/hides the IME; works around the Android quirk where
//                                  requestFocus() alone does not always raise the keyboard.
// String.toShortTableTitle       — extracts "Table 3" from a full chart title for compact display
//                                  in the bookmarks/saved list (e.g. "Table 3: High Prevalence…").
// Related: SavedFragment, GlobalSearchFragment, NotesFragment, BodyFragment.
package org.apphatchery.gatbreferenceguide.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * Registers a text-change callback on this [SearchView], ignoring submit events.
 * Normalises null to empty string so callers always receive a non-null value.
 */
fun SearchView.onQueryTextChange(onQueryTextChange: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?) = true

        override fun onQueryTextChange(newText: String?): Boolean {
            onQueryTextChange(newText.orEmpty())
            return true
        }
    })
}

/**
 * Provides left-swipe-to-delete for any RecyclerView.
 * [onTouchHelperCallback] receives the swiped adapter position; the caller is responsible
 * for removing the item from its data source and notifying the adapter.
 */
abstract class OnTouchHelper(val onTouchHelperCallback: (Int) -> Int) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = true

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onTouchHelperCallback(viewHolder.adapterPosition)
    }
}

fun EditText.setOnTextWatcher(
    onTextChangedListener: (String) -> Unit
) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(
            charSequence: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            charSequence?.let {
                if (it.isNotBlank()) onTextChangedListener(it.toString().trim())
            }

        }

        override fun afterTextChanged(s: Editable?) = Unit
    })
}


/**
 * Observes this [LiveData] for exactly one emission, then removes the observer.
 * Use this instead of a regular observe() when only the first DB result matters —
 * e.g. resolving a chapter entity to build a navigation argument without keeping
 * a permanent subscription that would re-trigger on unrelated DB writes.
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

fun EditText.onSearchKeyword(searchText: String) = apply {
    setText(searchText)
    requestFocus()
    setSelection(text.toString().length)
}

fun EditText.toggleSoftKeyboard(context: Context, showSoftKeyboard: Boolean = true) =
    context.apply {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (showSoftKeyboard) inputManager.showSoftInput(
            this@toggleSoftKeyboard,
            InputMethodManager.SHOW_IMPLICIT
        ) else {
            inputManager.hideSoftInputFromWindow(this@toggleSoftKeyboard.windowToken, 0)
        }
    }

fun getActionBar(context: Context) = (context as AppCompatActivity).supportActionBar

// Extract compact table title from a full title like
// "Table 3: High Prevalence and High-Risk Groups" -> "Table 3"
private val TABLE_SHORT_TITLE_REGEX =
    Regex("""\b(?:Table)\s*\d+[A-Za-z]?""", RegexOption.IGNORE_CASE)

fun String.toShortTableTitle(): String {
    val match = TABLE_SHORT_TITLE_REGEX.find(this) ?: return this
    val token = match.value
    // Ensure leading word has proper case
    return if (token.first().isLowerCase()) token.replaceFirstChar { it.titlecase() } else token
}


