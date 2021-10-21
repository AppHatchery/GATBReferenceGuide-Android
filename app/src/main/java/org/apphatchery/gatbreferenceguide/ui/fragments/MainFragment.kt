package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentMainBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChartAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAMainViewModel
import org.apphatchery.gatbreferenceguide.utils.getBottomNavigationView
import org.apphatchery.gatbreferenceguide.utils.toggleVisibility


@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main) {

    private lateinit var fragmentMainBinding: FragmentMainBinding
    private lateinit var first6ChapterAdapter: FAMainFirst6ChapterAdapter
    private lateinit var first6ChartAdapter: FAMainFirst6ChartAdapter
    private lateinit var predefinedChapterList: ArrayList<ChapterEntity>
    private lateinit var predefinedChartList: ArrayList<ChartAndSubChapter>

    private val viewModel: FAMainViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentMainBinding = FragmentMainBinding.bind(view)
        requireActivity().getBottomNavigationView().toggleVisibility(true)
        predefinedChapterList = ArrayList()
        predefinedChartList = ArrayList()

        first6ChapterAdapter = FAMainFirst6ChapterAdapter().also { adapter ->
            viewModel.getChapter.observe(viewLifecycleOwner) {
                with(predefinedChapterList) {
                    clear()
                    add(it[3].copy(chapterTitle = "Diagnosis for Active TB"))
                    add(it[4].copy(chapterTitle = "Treatment for Active TB"))
                    add(it[1].copy(chapterTitle = "Diagnosis for LTBI"))
                    add(it[2].copy(chapterTitle = "Treatment for LTBI"))
                    add(it[0])
                    adapter.submitList(this)
                }
            }

            adapter.itemClickCallback {
                MainFragmentDirections.actionMainFragmentToSubChapterFragment(it).apply {
                    findNavController().navigate(this)
                }
            }
        }


        first6ChartAdapter = FAMainFirst6ChartAdapter().also { adapter ->
            viewModel.getChart.observe(viewLifecycleOwner) { data ->
                with(predefinedChartList) {
                    clear()
                    add(data[19].copy(chartEntity = data[19].chartEntity.copy(chartTitle = "Use of TB drugs in Special Situations")))
                    add(data[13].copy(chartEntity = data[13].chartEntity.copy(chartTitle = "IV Therapy Drugs")))
                    add(data[14].copy(chartEntity = data[14].chartEntity.copy(chartTitle = "Alternative regimens")))
                    add(data[4].copy(chartEntity = data[4].chartEntity.copy(chartTitle = "Dosages for LTBI regimens")))
                    add(data[18].copy(chartEntity = data[18].chartEntity.copy(chartTitle = "Treatment of extrapulmonary TB")))
                    add(data[6].copy(chartEntity = data[6].chartEntity.copy(chartTitle = "Regimens for drug-susceptible TB pulmonary TB")))
                    adapter.submitList(this)
                }
            }

            adapter.itemClickCallback {
                viewModel.getChapterInfo(it.subChapterEntity.chapterId)
                    .observe(viewLifecycleOwner) { chapterEntity ->
                        MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                            BodyUrl(chapterEntity, it.subChapterEntity),
                            it
                        ).apply {
                            findNavController().navigate(this)
                        }
                    }
            }
        }



        fragmentMainBinding.apply {
            recyclerviewFirst6Chapters.setupAdapter(first6ChapterAdapter)
            recyclerviewFirst6Charts.setupAdapter(first6ChartAdapter, 3)

            searchView.setOnClickListener {
                MainFragmentDirections.actionGlobalGlobalSearchFragment().also {
                    findNavController().navigate(it)
                }
            }

            textviewAllChapters.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_chapterFragment)
            }

            textviewAllCharts.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_chartFragment)
            }
        }

    }

    private fun RecyclerView.setupAdapter(
        listAdapter: RecyclerView.Adapter<*>,
        spanCount: Int = 2
    ) {
        layoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter = listAdapter
    }

}