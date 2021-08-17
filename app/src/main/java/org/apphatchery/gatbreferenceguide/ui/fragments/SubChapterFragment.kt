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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentChapterAndSubChapterBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FASubChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASubChapterViewModel
import org.apphatchery.gatbreferenceguide.utils.onQueryTextChange
import org.apphatchery.gatbreferenceguide.utils.searchNotFound
import org.apphatchery.gatbreferenceguide.utils.setupToolbar

@AndroidEntryPoint
class SubChapterFragment : BaseFragment(R.layout.fragment_chapter_and_sub_chapter) {


    private lateinit var fragmentChapterAndSubChapterBinding: FragmentChapterAndSubChapterBinding
    private val subChapterFragmentArgs: SubChapterFragmentArgs by navArgs()
    private lateinit var faSubChapterAdapter: FASubChapterAdapter
    private lateinit var chapterEntity: ChapterEntity
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private val viewModel: FASubChapterViewModel by viewModels()


    private lateinit var searchView: SearchView
    private lateinit var menuItem: MenuItem


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentChapterAndSubChapterBinding = FragmentChapterAndSubChapterBinding.bind(view)


        chapterEntity = subChapterFragmentArgs.chapterEntity
        viewModel.chapterId = chapterEntity.chapterId


        faSubChapterAdapter = FASubChapterAdapter()
        viewModel.getSubChapterEntity.observe(viewLifecycleOwner) {
            fragmentChapterAndSubChapterBinding.apply {
                it.size.searchNotFound(recyclerview, searchNotFound)
                faSubChapterAdapter.submitList(it)
            }
        }


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                voiceSearchForActivityResult(it)
            }

        faSubChapterAdapter.itemClickCallback {
            val subChapterFragmentDirections =
                SubChapterFragmentDirections.actionSubChapterFragmentToBodyFragment(
                    BodyUrl(chapterEntity, it)
                )

            findNavController().navigate(subChapterFragmentDirections)
        }


        fragmentChapterAndSubChapterBinding.apply {
            toolbar.setupToolbar(requireActivity(), chapterEntity.chapterTitle)

            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faSubChapterAdapter
            }
        }

        setHasOptionsMenu(true)

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