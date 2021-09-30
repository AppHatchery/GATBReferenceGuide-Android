package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAChartAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAChartViewModel
import org.apphatchery.gatbreferenceguide.utils.TAG
import org.apphatchery.gatbreferenceguide.utils.enableToolbar

@AndroidEntryPoint
class ChartFragment : BaseFragment(R.layout.fragment_with_recyclerview) {


    private lateinit var bind: FragmentWithRecyclerviewBinding
    private lateinit var faChartAdapter: FAChartAdapter
    private val viewModel: FAChartViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWithRecyclerviewBinding.bind(view)
        faChartAdapter = FAChartAdapter().also { faChartAdapter ->
            viewModel.getChart.observe(viewLifecycleOwner) {
                faChartAdapter.submitList(it)
            }

            faChartAdapter.itemClickCallback {
                viewModel.getChapterInfo(it.subChapterEntity.chapterId).observe(viewLifecycleOwner){ chapterEntity->
                    ChartFragmentDirections.actionChartFragmentToBodyFragment(
                        BodyUrl(chapterEntity, it.subChapterEntity),
                        it
                    ).apply {
                        findNavController().navigate(this)
                    }
                }

            }
        }

        bind.apply {

            toolbarBackButton.setOnClickListener { requireActivity().onBackPressed() }
            "All Charts".also { toolbarTitle.text = it }
            toolbar.enableToolbar(requireActivity())


            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faChartAdapter
            }
        }


        setHasOptionsMenu(true)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.searchView) SubChapterFragmentDirections.actionGlobalGlobalSearchFragment()
            .also { findNavController().navigate(it) }
        return super.onOptionsItemSelected(item)
    }


}