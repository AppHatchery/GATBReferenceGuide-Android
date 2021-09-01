package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paulrybitskyi.persistentsearchview.adapters.model.SuggestionItem
import com.paulrybitskyi.persistentsearchview.utils.SuggestionCreationUtil
import com.paulrybitskyi.persistentsearchview.utils.VoiceRecognitionDelegate
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentMainBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChartAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAMainViewModel
import org.apphatchery.gatbreferenceguide.utils.onQueryTextChange
import org.apphatchery.gatbreferenceguide.utils.setupToolbar


@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main) {

    private lateinit var fragmentMainBinding: FragmentMainBinding
    private lateinit var first6ChapterAdapter: FAMainFirst6ChapterAdapter
    private lateinit var first6ChartAdapter: FAMainFirst6ChartAdapter
    private lateinit var chapterList: ArrayList<ChapterEntity>

    private val viewModel: FAMainViewModel by viewModels()




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentMainBinding = FragmentMainBinding.bind(view)
//        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
        chapterList = ArrayList()

        first6ChapterAdapter = FAMainFirst6ChapterAdapter().also { adapter ->
            viewModel.getChapter.observe(viewLifecycleOwner) {
                adapter.submitList(it.subList(0, 5))
                chapterList.addAll(it)
            }

            adapter.itemClickCallback {
                viewModel.getCountByChapterId(it.chapterId).observe(viewLifecycleOwner) { count ->
                    findNavController().navigate(
                        if (count == 0) MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                            BodyUrl(it), null
                        ) else MainFragmentDirections.actionMainFragmentToSubChapterFragment(it)
                    )
                }
            }
        }





        first6ChartAdapter = FAMainFirst6ChartAdapter().also { adapter ->
            viewModel.getChart.observe(viewLifecycleOwner) { data ->
                adapter.submitList(data.subList(0,6))
            }

            adapter.itemClickCallback {
                val directions = MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                    BodyUrl(ChapterEntity(it.subChapterEntity.chapterId, ""), it.subChapterEntity),
                    it
                )
                findNavController().navigate(directions)
            }
        }



        fragmentMainBinding.apply {
            recyclerviewFirst6Chapters.setupAdapter(first6ChapterAdapter)
            recyclerviewFirst6Charts.setupAdapter(first6ChartAdapter, 3)
            toolbar.setupToolbar(requireActivity(), "Guide", null, false)

            textviewAllChapters.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_chapterFragment)
            }

            textviewAllCharts.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_chartFragment)
            }
        }


        setHasOptionsMenu(true)
    }

    private fun RecyclerView.setupAdapter(listAdapter: RecyclerView.Adapter<*>, spanCount: Int = 2) {
        layoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter = listAdapter
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.searchView -> {
                val directions = MainFragmentDirections.actionGlobalGlobalSearchFragment()
                findNavController().navigate(directions)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_main_menu, menu)
    }

}