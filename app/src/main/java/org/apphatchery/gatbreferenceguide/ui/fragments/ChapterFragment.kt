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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWithRecyclerviewBinding
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAChapterViewModel
import org.apphatchery.gatbreferenceguide.utils.getBottomNavigationView
import org.apphatchery.gatbreferenceguide.utils.isChecked
import org.apphatchery.gatbreferenceguide.utils.navigateSafe
import org.apphatchery.gatbreferenceguide.utils.searchState


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChapterFragment : BaseFragment(R.layout.fragment_with_recyclerview) {

    private lateinit var bind: FragmentWithRecyclerviewBinding
    private lateinit var faChapterAdapter: FAChapterAdapter
    private val viewModel: FAChapterViewModel by viewModels()
    private var hadData = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWithRecyclerviewBinding.bind(view)


        faChapterAdapter = FAChapterAdapter().also {
            viewModel.getChapterEntity.observe(viewLifecycleOwner) { data ->
                if (data.isNotEmpty()) hadData = true
                if (hadData && data.isEmpty()) {
                    Log.w("ChapterFragment", "Chapter list became empty after having data")
                }
                bind.apply {
//                    data.size.searchNotFound(recyclerview, searchNotFound)
                    it.submitList(data)
                }
            }
        }


        bind.apply {
             recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faChapterAdapter
            }
        }
        faChapterAdapter.itemClickCallback {
            findNavController().navigateSafe(
                ChapterFragmentDirections.actionChapterFragmentToSubChapterFragment(it)
            )
        }
        requireActivity().getBottomNavigationView()?.isChecked(R.id.mainFragment)
    }


}