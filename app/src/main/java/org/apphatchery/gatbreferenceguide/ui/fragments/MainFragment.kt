package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChartAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAMainViewModel


@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main) {

    private lateinit var fragmentMainBinding: FragmentMainBinding
    private lateinit var first6ChapterAdapter: FAMainFirst6ChapterAdapter
    private lateinit var first6ChartAdapter: FAMainFirst6ChartAdapter
    private lateinit var chapterList: ArrayList<ChapterEntity>

    private val viewModel: FAMainViewModel by viewModels()


    private fun regularSuggestion(): MutableList<SuggestionItem> {
        with(ArrayList<String>()) {
            chapterList.forEach {
                add(it.chapterTitle)
            }
            return SuggestionCreationUtil.asRegularSearchSuggestions(this)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentMainBinding = FragmentMainBinding.bind(view)
//        requireActivity().findViewById<View>(R.id.bottomNavigationView).visibility = View.VISIBLE
        chapterList = ArrayList()

        first6ChapterAdapter = FAMainFirst6ChapterAdapter().also { adapter ->
            viewModel.getChapter.observe(viewLifecycleOwner) {
                adapter.submitList(it.subList(0, 6))
                chapterList.addAll(it)
                fragmentMainBinding.persistentSearchView.setSuggestions(regularSuggestion(), false)
            }

            adapter.itemClickCallback {
                viewModel.getCountByChapterId(it.chapterId).observe(viewLifecycleOwner) { count ->
                    findNavController().navigate(
                        if (count == 0) MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                            BodyUrl(it)
                        ) else MainFragmentDirections.actionMainFragmentToSubChapterFragment(it)
                    )
                }
            }
        }





        first6ChartAdapter = FAMainFirst6ChartAdapter().also {
            viewModel.getChart.observe(viewLifecycleOwner) { data ->
                it.submitList(data)
            }


        }



        fragmentMainBinding.apply {
            recyclerviewFirst6Chapters.setupAdapter(first6ChapterAdapter)
            recyclerviewFirst6Charts.setupAdapter(first6ChartAdapter)

            textviewAllChapters.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_chapterFragment)
            }

            persistentSearchView
                .setVoiceRecognitionDelegate(VoiceRecognitionDelegate(this@MainFragment))
        }


        setHasOptionsMenu(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        VoiceRecognitionDelegate.handleResult(
            fragmentMainBinding.persistentSearchView,
            requestCode,
            resultCode,
            data
        )
    }

    private fun RecyclerView.setupAdapter(listAdapter: RecyclerView.Adapter<*>) {
        layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = listAdapter
    }

}