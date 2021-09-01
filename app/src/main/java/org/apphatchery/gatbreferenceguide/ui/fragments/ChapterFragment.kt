package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAChapterViewModel
import org.apphatchery.gatbreferenceguide.utils.enableToolbar


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChapterFragment : BaseFragment(R.layout.fragment_with_recyclerview) {

    private lateinit var bind: FragmentWithRecyclerviewBinding
    private lateinit var faChapterAdapter: FAChapterAdapter
    private val viewModel: FAChapterViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWithRecyclerviewBinding.bind(view)
        setHasOptionsMenu(true)

        faChapterAdapter = FAChapterAdapter().also {
            viewModel.getChapterEntity.observe(viewLifecycleOwner) { data ->
                bind.apply {
//                    data.size.searchNotFound(recyclerview, searchNotFound)
                    it.submitList(data)
                }
            }
        }


        bind.apply {

            toolbarBackButton.setOnClickListener { requireActivity().onBackPressed() }
            "All Chapters".also { toolbarTitle.text = it }
            toolbar.enableToolbar(requireActivity())
            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faChapterAdapter
            }
        }
        faChapterAdapter.itemClickCallback {
            viewModel.getCountByChapterId(it.chapterId).observe(viewLifecycleOwner) { count ->
                findNavController().navigate(
                    if (count == 0) ChapterFragmentDirections.actionChapterFragmentToBodyFragment(
                        BodyUrl(it), null
                    ) else ChapterFragmentDirections.actionChapterFragmentToSubChapterFragment(it)
                )
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.searchView -> {
                val directions = ChapterFragmentDirections.actionGlobalGlobalSearchFragment()
                findNavController().navigate(directions)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}