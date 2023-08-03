package org.apphatchery.gatbreferenceguide.ui.fragments

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentGlobalSearchBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAGlobalSearchAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAGlobalSearchViewModel
import org.apphatchery.gatbreferenceguide.utils.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GlobalSearchFragment : BaseFragment(R.layout.fragment_global_search) {

    private lateinit var bind: FragmentGlobalSearchBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: FAGlobalSearchViewModel by viewModels()

    @Inject
    lateinit var faGlobalSearchAdapter: FAGlobalSearchAdapter

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentGlobalSearchBinding.bind(view)


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                voiceSearchForActivityResult(it) { searchText ->
                    bind.searchKeyword.onSearchKeyword(searchText)
                }
            }
        var count = 1
        faGlobalSearchAdapter.also { faGlobalSearchAdapter ->
            // Inside your Fragment or Activity
            viewModel.getGlobalSearchEntity.observe(viewLifecycleOwner) { word ->

                // Run the search and highlighting process in a coroutine tied to the lifecycle of the component
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    // Get the search query only once outside of the coroutine
                    val search = viewModel.searchQuery
                    val searchWords = search.value.split("\\s+".toRegex())

                    val highlightedWord = word.map { item ->
                        val highlightedTextInBody = searchWords.fold(item.textInBody) { acc, wordToHighlight ->
                            highlightWord(acc, wordToHighlight)
                        }
                        calculatePercentage(word.size.toDouble(), count++.toDouble())


                        item.copy(
                            subChapter = item.subChapter,
                            searchTitle = item.searchTitle,
                            textInBody = highlightedTextInBody
                        )

                    }

                    // Update the UI on the main thread with the results.
                    withContext(Dispatchers.Main) {
                        faGlobalSearchAdapter.submitList(highlightedWord)
                        highlightedWord.size.noItemFound(bind.visibleViewGroup, bind.noItemFound)
                        bind.progressBar.visibility = View.GONE
                        bind.percentIndicator.visibility = View.GONE
                        "${highlightedWord.size} result${if (highlightedWord.size == 1) "" else "s"}".also {
                            bind.searchItemCount.text = it
                        }
                    }
                }
            }







            faGlobalSearchAdapter.itemClickCallback {
                bind.searchKeyword.toggleSoftKeyboard(requireContext(), false)

                /*Log search keyword name*/
                firebaseAnalytics.logEvent(
                    ANALYTICS_SEARCH_EVENT,
                    bundleOf(Pair(ANALYTICS_SEARCH_EVENT, bind.searchKeyword.text.toString()))
                )
                val cleanSearchString = if (bind.searchKeyword.text.toString()
                        .isNotEmpty() && bind.searchKeyword.text.toString() != " "
                ) bind.searchKeyword.text.toString() else ""
                viewModel.getSubChapterById(it.subChapterId.toString())
                    .observe(viewLifecycleOwner) { subChapter ->
                        GlobalSearchFragmentDirections.actionGlobalSearchFragmentToBodyFragment(
                            BodyUrl(
                                ChapterEntity(it.chapterId, it.searchTitle),
                                subChapter,
                                cleanSearchString
                            ), null
                        ).also { findNavController().navigate(it) }
                        bind.searchKeyword.clearFocus()
                    }
            }

        }

        bind.apply {
            CoroutineScope(Dispatchers.Main).launch {
                recyclerview.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = faGlobalSearchAdapter
                }

                voiceSearch.setOnClickListener {
                    voiceSearchListener(resultLauncher)
                }

                searchKeyword.setOnTextWatcher(
                    onTextChangedListener = {
                        viewModel.searchQuery.value = it

                        with(bind.searchItemCount) {
                            visibility = if (searchKeyword.text.toString().trim()
                                    .isBlank()
                            ) View.GONE else View.VISIBLE
                        }
                    })
                searchKeyword.doAfterTextChanged { editable ->
                    if (editable != null) {
                        if (editable.isBlank()) {
                            viewModel.searchQuery.value = ""
                        }
                    }
                }
            }
        }

        bind.searchKeyword.apply {
            requestFocus()
            toggleSoftKeyboard(requireContext())

        }

    }
    // Utilize a custom extension function for highlighting
    fun highlightWord(original: String, wordToHighlight: String): String {
        val regex = Regex("(?i)\\b${Regex.escape(wordToHighlight)}\\b")
        return original.replace(regex) {
            "<span style='background-color: yellow; color: black; font-weight: bold;'>${it.value}</span>"
        }
    }
    fun calculatePercentage(finalValue: Double, changingValue: Double){
        // Ensure the values are within the valid range
        val validFinalValue = finalValue.coerceAtLeast(0.0)
        val validChangingValue = changingValue.coerceAtLeast(0.0).coerceAtMost(validFinalValue)

        // Calculate the percentage completion

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main) {
                if (isDarkModeEnabled(requireContext())){
                    bind.percentIndicator.setTextColor(Color.WHITE)
                }
                bind.percentIndicator.text = ((validChangingValue / validFinalValue) * 100.0).toInt().toString()
            }
        }

    }

    fun isDarkModeEnabled(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        return uiModeManager?.nightMode == Configuration.UI_MODE_NIGHT_YES
    }


    override fun onDestroyView() {
        val activity = requireActivity()
        val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)

        super.onDestroyView()
    }

}
