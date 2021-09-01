package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FASubChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASubChapterViewModel
import org.apphatchery.gatbreferenceguide.utils.enableToolbar

@AndroidEntryPoint
class SubChapterFragment : BaseFragment(R.layout.fragment_with_recyclerview) {


    private lateinit var bind: FragmentWithRecyclerviewBinding
    private val subChapterFragmentArgs: SubChapterFragmentArgs by navArgs()
    private lateinit var faSubChapterAdapter: FASubChapterAdapter
    private lateinit var chapterEntity: ChapterEntity
    private val viewModel: FASubChapterViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWithRecyclerviewBinding.bind(view)
        chapterEntity = subChapterFragmentArgs.chapterEntity
        viewModel.chapterId = chapterEntity.chapterId


        faSubChapterAdapter = FASubChapterAdapter()
        viewModel.getSubChapterEntity.observe(viewLifecycleOwner) {
            bind.apply {
//                it.size.searchNotFound(recyclerview, searchNotFound)
                faSubChapterAdapter.submitList(it)
            }
        }


        faSubChapterAdapter.itemClickCallback {
            val subChapterFragmentDirections =
                SubChapterFragmentDirections.actionSubChapterFragmentToBodyFragment(
                    BodyUrl(chapterEntity, it), null
                )

            findNavController().navigate(subChapterFragmentDirections)
        }


        bind.apply {

            toolbarBackButton.setOnClickListener { requireActivity().onBackPressed() }
            chapterEntity.chapterTitle.also { toolbarTitle.text = it }
            toolbar.enableToolbar(requireActivity())

            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faSubChapterAdapter
            }
        }

        setHasOptionsMenu(true)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.searchView -> {
                val directions = SubChapterFragmentDirections.actionGlobalGlobalSearchFragment()
                findNavController().navigate(directions)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}