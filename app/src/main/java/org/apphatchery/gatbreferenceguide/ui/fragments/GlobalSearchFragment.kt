package org.apphatchery.gatbreferenceguide.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

        faGlobalSearchAdapter.also { faGlobalSearchAdapter ->
            viewModel.getGlobalSearchEntity.observe(viewLifecycleOwner) {word->

                val search = viewModel.searchQuery
                val highlightedWord = word.map { item ->
                    val highlightSearchTerm = "<font color='Red'>${search.value}</font>"
                    val highlightedSearchChapter = item.subChapter.replace(search.value, highlightSearchTerm, ignoreCase = true)
                    val highlightedSearchTitle = item.searchTitle.replace(search.value, highlightSearchTerm, ignoreCase = true)
                    val highlightedTextInBody = item.textInBody.replace(search.value, highlightSearchTerm, ignoreCase = true)
                    item.copy(searchTitle = highlightedSearchTitle, textInBody = highlightedTextInBody, subChapter = highlightedSearchChapter)
                }

                //faGlobalSearchAdapter.submitList(highlightedWord)

                faGlobalSearchAdapter.submitList(highlightedWord)
                highlightedWord.size.noItemFound(bind.visibleViewGroup, bind.noItemFound)
                "${highlightedWord.size} result${if (highlightedWord.size == 1) "" else "s"}".also {
                    bind.searchItemCount.text = it
                }
            }


            faGlobalSearchAdapter.itemClickCallback {

                /*Log search keyword name*/
                firebaseAnalytics.logEvent(
                    ANALYTICS_SEARCH_EVENT,
                    bundleOf(Pair(ANALYTICS_SEARCH_EVENT, bind.searchKeyword.text.toString()))
                )
                val cleanSearchString = if(bind.searchKeyword.text.toString().isNotEmpty() && bind.searchKeyword.text.toString() != " " ) bind.searchKeyword.text.toString() else ""
                viewModel.getSubChapterById(it.subChapterId.toString())
                    .observe(viewLifecycleOwner) { subChapter ->
                        GlobalSearchFragmentDirections.actionGlobalSearchFragmentToBodyFragment(
                            BodyUrl(
                                ChapterEntity(it.chapterId, it.searchTitle), subChapter, cleanSearchString
                            ), null
                        ).also { findNavController().navigate(it) }
                        bind.searchKeyword.clearFocus()
                    }
            }

        }

        bind.apply {
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

        bind.searchKeyword.apply {
            requestFocus()
            toggleSoftKeyboard(requireContext())

        }

    }


    override fun onDestroyView() {
        bind.searchKeyword.clearFocus()
        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager).apply {
            hideSoftInputFromWindow(bind.searchKeyword.windowToken, 0)
        }
        super.onDestroyView()
    }


}
