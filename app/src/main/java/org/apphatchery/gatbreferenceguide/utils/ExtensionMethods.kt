package org.apphatchery.gatbreferenceguide.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.paulrybitskyi.persistentsearchview.PersistentSearchView
import com.paulrybitskyi.persistentsearchview.adapters.model.SuggestionItem
import com.paulrybitskyi.persistentsearchview.listeners.OnSuggestionChangeListener


fun SearchView.onQueryTextChange(onQueryTextChange: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?) = true

        override fun onQueryTextChange(newText: String?): Boolean {
            onQueryTextChange(newText.orEmpty())
            return true
        }
    })
}


fun PersistentSearchView.onSuggestionListener(onSuggestionLister: (SuggestionItem) -> Unit) {
    setOnSuggestionChangeListener(object : OnSuggestionChangeListener {
        override fun onSuggestionPicked(suggestion: SuggestionItem) {
            onSuggestionLister(suggestion)
        }

        override fun onSuggestionRemoved(suggestion: SuggestionItem?) {

        }
    })
}

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
    onTextChangedListener: (String) -> Unit,
    afterTextChangedListener: () -> Unit
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
                if (it.isNotBlank()) onTextChangedListener(it.toString().lowercase().trim())
            }

        }

        override fun afterTextChanged(s: Editable?) = afterTextChangedListener()
    })
}


fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}