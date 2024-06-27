package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
    private val viewModel: FASubChapterViewModel by viewModels()
    private val mainViewModel: MainActivityViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWithRecyclerviewBinding.bind(view)
        chapterEntity = subChapterFragmentArgs.chapterEntity
        viewModel.chapterId = chapterEntity.chapterId


        faSubChapterAdapter = FASubChapterAdapter()
        viewModel.getSubChapterEntity.observe(viewLifecycleOwner) {
            bind.apply {
                faSubChapterAdapter.submitList(it)
            }
        }


        faSubChapterAdapter.itemClickCallback {
            val subChapterFragmentDirections =
                SubChapterFragmentDirections.actionSubChapterFragmentToBodyFragment(
                    BodyUrl(chapterEntity, it, ""), null
                )

            findNavController().navigate(subChapterFragmentDirections)
        }


        bind.apply {

            getActionBar(requireActivity())?.title = chapterEntity.chapterTitle
            mainViewModel.title.value = HtmlCompat.fromHtml(chapterEntity.chapterTitle, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faSubChapterAdapter
            }
        }

        setHasOptionsMenu(true)

        requireActivity().getBottomNavigationView()?.isChecked(R.id.mainFragment)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        if(searchState.currentState.toString() == "IN_SEARCH"){
            if (item.itemId == R.id.searchView) {
                var comp =   findNavController().popBackStack(R.id.globalSearchFragment,false)
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

        return super.onOptionsItemSelected(item)
    }


}