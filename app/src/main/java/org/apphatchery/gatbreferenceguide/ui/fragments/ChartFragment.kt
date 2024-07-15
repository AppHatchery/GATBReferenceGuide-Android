package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAChartAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAChartViewModel
import org.apphatchery.gatbreferenceguide.utils.getBottomNavigationView
import org.apphatchery.gatbreferenceguide.utils.isChecked
import org.apphatchery.gatbreferenceguide.utils.searchState

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
                viewModel.getChapterInfo(it.subChapterEntity.chapterId)
                    .observe(viewLifecycleOwner) { chapterEntity ->
                        ChartFragmentDirections.actionChartFragmentToBodyFragment(
                            BodyUrl(chapterEntity, it.subChapterEntity, ""),
                            it
                        ).apply {
                            findNavController().navigate(this)
                        }
                    }

            }
        }

        bind.apply {

            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faChartAdapter
            }
        }




        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return handleMenuItemSelection(menuItem)
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        requireActivity().getBottomNavigationView()?.isChecked(R.id.mainFragment)
    }

    private fun handleMenuItemSelection(item: MenuItem): Boolean {
                if(searchState.currentState.toString() == "IN_SEARCH"){
            if (item.itemId == R.id.searchView) {
                val comp =   findNavController().popBackStack(R.id.globalSearchFragment,false)
                if(!comp){
                    if (item.itemId == R.id.searchView) SubChapterFragmentDirections.actionGlobalGlobalSearchFragment()
                        .also {
                            findNavController().navigate(it)
                        }
                }
            }

        }else{
            if (item.itemId == R.id.searchView) SubChapterFragmentDirections.actionGlobalGlobalSearchFragment()
                .also {
                    findNavController().navigate(it)
                }
        }

        return false
    }


}