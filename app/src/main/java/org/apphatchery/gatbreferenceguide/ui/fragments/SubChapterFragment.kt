package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
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
import org.apphatchery.gatbreferenceguide.ui.viewmodels.MainActivityViewModel
import org.apphatchery.gatbreferenceguide.utils.getActionBar
import org.apphatchery.gatbreferenceguide.utils.getBottomNavigationView
import org.apphatchery.gatbreferenceguide.utils.isChecked
import org.apphatchery.gatbreferenceguide.utils.searchState

@AndroidEntryPoint
class SubChapterFragment : BaseFragment(R.layout.fragment_with_recyclerview) {


    private lateinit var bind: FragmentWithRecyclerviewBinding
    private val subChapterFragmentArgs: SubChapterFragmentArgs by navArgs()
    private lateinit var faSubChapterAdapter: FASubChapterAdapter
    private lateinit var chapterEntity: ChapterEntity
    private var resolvedChapterEntity: ChapterEntity? = null
    private val viewModel: FASubChapterViewModel by viewModels()
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private var hadData = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWithRecyclerviewBinding.bind(view)
        chapterEntity = subChapterFragmentArgs.chapterEntity
        viewModel.chapterId = chapterEntity.chapterId

        // Always set toolbar title from the DB-backed chapter name (not the Home shortcut label).
        viewModel.getChapterInfo(chapterEntity.chapterId).observe(viewLifecycleOwner) { dbChapter ->
            resolvedChapterEntity = dbChapter
            setActionBarTitle(dbChapter.chapterTitle)
        }


        faSubChapterAdapter = FASubChapterAdapter()
        viewModel.getSubChapterEntity.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) hadData = true
            if (hadData && it.isEmpty()) {
                Log.w("SubChapterFragment", "SubChapter list became empty after having data")
            }
            bind.apply {
                faSubChapterAdapter.submitList(it)
            }
        }


        faSubChapterAdapter.itemClickCallback {
            val chapterForNav = resolvedChapterEntity ?: chapterEntity
            val subChapterFragmentDirections =
                SubChapterFragmentDirections.actionSubChapterFragmentToBodyFragment(
                    BodyUrl(chapterForNav, it, ""), null
                )

            findNavController().navigate(subChapterFragmentDirections)
        }


        bind.apply {

            // Title is set via DB observation above.

            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faSubChapterAdapter
            }
        }


        // Removed this entire search menu block:
        /*
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return handleMenuItemSelection(menuItem)
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        */

        requireActivity().getBottomNavigationView()?.isChecked(R.id.mainFragment)
    }

}