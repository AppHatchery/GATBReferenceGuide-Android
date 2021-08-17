package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentChapterAndSubChapterBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAChapterViewModel
import org.apphatchery.gatbreferenceguide.utils.onQueryTextChange
import org.apphatchery.gatbreferenceguide.utils.searchNotFound
import org.apphatchery.gatbreferenceguide.utils.setupToolbar


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChapterFragment : BaseFragment(R.layout.fragment_chapter_and_sub_chapter) {

    private lateinit var fragmentChapterAndSubChapterBinding: FragmentChapterAndSubChapterBinding
    private lateinit var faChapterAdapter: FAChapterAdapter
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private val viewModel: FAChapterViewModel by viewModels()

    private lateinit var searchView: SearchView
    private lateinit var menuItem: MenuItem



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentChapterAndSubChapterBinding = FragmentChapterAndSubChapterBinding.bind(view)
        setHasOptionsMenu(true)

        faChapterAdapter = FAChapterAdapter().also {
            viewModel.getChapterEntity.observe(viewLifecycleOwner) { data ->
                fragmentChapterAndSubChapterBinding.apply {
                    data.size.searchNotFound(recyclerview, searchNotFound)
                    it.submitList(data)
                }
            }
        }


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                voiceSearchForActivityResult(it)
            }


        fragmentChapterAndSubChapterBinding.apply {
            toolbar.setupToolbar(requireActivity(), "All Chapters")
            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faChapterAdapter
            }
        }
        faChapterAdapter.itemClickCallback {
            viewModel.getCountByChapterId(it.chapterId).observe(viewLifecycleOwner) { count ->
                findNavController().navigate(
                    if (count == 0) ChapterFragmentDirections.actionChapterFragmentToBodyFragment(
                        BodyUrl(it)
                    ) else ChapterFragmentDirections.actionChapterFragmentToSubChapterFragment(it)
                )
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        menuItem = menu.findItem(R.id.searchView)
        searchView = menuItem.actionView as SearchView
        searchView.onQueryTextChange {
            viewModel.searchQuery.value = it
        }
    }


    private fun voiceSearchListener() {
        resultLauncher.launch(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
                it.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            })
    }

    private fun voiceSearchForActivityResult(activityResult: ActivityResult) {
        val matches = activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (matches != null && matches.size > 0) {
            val searchWrd = matches[0]
            if (!TextUtils.isEmpty(searchWrd)) {
                menuItem.expandActionView()
                searchView.setQuery(searchWrd, false)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.voiceSearch -> voiceSearchListener()
        }
        return super.onOptionsItemSelected(item)
    }

}