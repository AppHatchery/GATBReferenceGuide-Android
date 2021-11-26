package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
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
import org.apphatchery.gatbreferenceguide.utils.getActionBar
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
        getActionBar(requireActivity())?.setDisplayHomeAsUpEnabled(false)
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
                    add(data[7].copy(chartEntity = data[7].chartEntity.copy(chartTitle = "First Line TB Drugs for Adults")))
                    add(data[13].copy(chartEntity = data[13].chartEntity.copy(chartTitle = "IV Therapy Drugs")))
                    add(data[14].copy(chartEntity = data[14].chartEntity.copy(chartTitle = "Alternative Regimens")))
                    add(data[4].copy(chartEntity = data[4].chartEntity.copy(chartTitle = "Dosages for LTBI Regimens")))
                    add(data[18].copy(chartEntity = data[18].chartEntity.copy(chartTitle = "Treatment of Extrapulmonary TB")))
                    add(data[19].copy(chartEntity = data[19].chartEntity.copy(chartTitle = "Use of TB drugs in Special Situations")))
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

        setupDynamicLink()

    }

    private fun RecyclerView.setupAdapter(
        listAdapter: RecyclerView.Adapter<*>,
        spanCount: Int = 2,
    ) {
        layoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter = listAdapter
    }

    private fun setupDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(requireActivity().intent)
            .addOnSuccessListener(requireActivity()) { pendingDynamicLink ->
                if (pendingDynamicLink != null)
                    pendingDynamicLink.link?.let {
                        val androidQueryId = it.getQueryParameter("androidQueryId")
                        val androidIsPage = it.getQueryParameter("androidIsPage")
                        if (androidIsPage != null && androidQueryId != null) {
                            requireActivity().intent.data = null
                            requireActivity().intent.replaceExtras(Bundle())
                            handleDynamicLink(androidIsPage.toInt(), androidQueryId)
                        }
                    }
            }
    }

    private fun handleDynamicLink(isPage: Int, id: String) {
        if (isPage == 0) {
            viewModel.getChartAndSubChapterById(id)
                .observe(viewLifecycleOwner) { chartAndSubchapter ->
                    viewModel.getSubChapterInfo(chartAndSubchapter.subChapterEntity.subChapterId.toString())
                        .observe(viewLifecycleOwner) { subChapterEntity ->
                            viewModel.getChapterInfo(subChapterEntity.chapterId)
                                .observe(viewLifecycleOwner) { chapterEntity ->
                                    MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                                        BodyUrl(chapterEntity, subChapterEntity), chartAndSubchapter
                                    ).apply {
                                        findNavController().navigate(this)
                                    }
                                }
                        }
                }
        } else {
            viewModel.getSubChapterInfo(id)
                .observe(viewLifecycleOwner) { subChapterEntity ->
                    viewModel.getChapterInfo(subChapterEntity.chapterId)
                        .observe(viewLifecycleOwner) { chapterEntity ->
                            MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                                BodyUrl(chapterEntity, subChapterEntity), null
                            ).apply {
                                findNavController().navigate(this)
                            }
                        }
                }

        }
    }
}