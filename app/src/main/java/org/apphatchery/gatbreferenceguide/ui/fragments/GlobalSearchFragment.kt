package org.apphatchery.gatbreferenceguide.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentGlobalSearchBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAGlobalSearchAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAGlobalSearchViewModel
import org.apphatchery.gatbreferenceguide.utils.enableToolbar
import org.apphatchery.gatbreferenceguide.utils.noItemFound
import org.apphatchery.gatbreferenceguide.utils.onSearchKeyword
import org.apphatchery.gatbreferenceguide.utils.setOnTextWatcher
import javax.inject.Inject


@AndroidEntryPoint
class GlobalSearchFragment : BaseFragment(R.layout.fragment_global_search) {

    private lateinit var bind: FragmentGlobalSearchBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: FAGlobalSearchViewModel by viewModels()

    @Inject
    lateinit var faGlobalSearchAdapter: FAGlobalSearchAdapter


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
            viewModel.getGlobalSearchEntity.observe(viewLifecycleOwner) {
                faGlobalSearchAdapter.submitList(it)
                it.size.noItemFound(bind.visibleViewGroup, bind.noItemFound)
                "(${it.size}) result(s) found".also { bind.searchItemCount.text = it }
            }


            faGlobalSearchAdapter.itemClickCallback {
                GlobalSearchFragmentDirections.actionGlobalSearchFragmentToBodyFragment(
                    BodyUrl(
                        ChapterEntity(it.chapterId, it.searchTitle),
                        SubChapterEntity(
                            it.subChapterId,
                            it.chapterId,
                            it.subChapter,
                            it.fileName
                        )
                    ), null
                ).also { findNavController().navigate(it) }
                bind.searchKeyword.clearFocus()
            }
        }

        bind.apply {
            toolbarBackButton.setOnClickListener { requireActivity().onBackPressed() }
            "Search Everywhere".also { toolbarTitle.text = it }
            toolbar.enableToolbar(requireActivity())
            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faGlobalSearchAdapter
            }

            searchKeyword.setOnTextWatcher(
                onTextChangedListener = {
                    viewModel.searchQuery.value = it
                },
                afterTextChangedListener = {
                    faGlobalSearchAdapter.searchQuery = searchKeyword.text.toString().lowercase()
                    faGlobalSearchAdapter.notifyDataSetChanged()
                }
            )

        }

        setHasOptionsMenu(true)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.voiceSearch) voiceSearchListener(resultLauncher)
        return super.onOptionsItemSelected(item)
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
