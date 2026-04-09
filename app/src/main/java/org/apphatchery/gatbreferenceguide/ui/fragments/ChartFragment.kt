// Fragment displaying the full list of TB reference charts/tables from the guide.
// Charts are fetched via FAChartViewModel.getChart (LiveData<List<ChartAndSubChapter>>) and shown
// in a RecyclerView with FAChartAdapter. Each item represents a medical reference table such as
// drug dosages, HIV co-infection protocols, or extrapulmonary TB treatment guidelines.
//
// Item click flow: tapping a chart row requires the parent ChapterEntity to build a complete
// BodyUrl navigation payload. The fragment calls viewModel.getChapterInfo(chapterId) to fetch it,
// then navigates with SafeArgs: ChartFragmentDirections.actionChartFragmentToBodyFragment().
// BodyFragment receives both the BodyUrl (chapter/section context) and the ChartAndSubChapter.
//
// hadData guard: tracks whether the list was ever non-empty. If it becomes empty after having
// data, a warning is logged — this indicates the DB was wiped but content wasn't re-seeded, which
// is a data integrity issue that should be investigated rather than silently ignored.
//
// Bottom nav: isChecked(R.id.mainFragment) keeps the Home icon highlighted during chart browsing.
// Layout: fragment_with_recyclerview (shared with ChapterFragment and SubChapterFragment).
// Related: FAChartViewModel, FAChartAdapter, BodyFragment, BodyUrl, ChartAndSubChapter, BodyUrl.
package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
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

@AndroidEntryPoint
class ChartFragment : BaseFragment(R.layout.fragment_with_recyclerview) {


    private lateinit var bind: FragmentWithRecyclerviewBinding
    private lateinit var faChartAdapter: FAChartAdapter
    private val viewModel: FAChartViewModel by viewModels()
    private var hadData = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWithRecyclerviewBinding.bind(view)
        faChartAdapter = FAChartAdapter().also { faChartAdapter ->
            viewModel.getChart.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) hadData = true
                if (hadData && it.isEmpty()) {
                    Log.w("ChartFragment", "Chart list became empty after having data")
                }
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

        requireActivity().getBottomNavigationView()?.isChecked(R.id.mainFragment)
    }


}
