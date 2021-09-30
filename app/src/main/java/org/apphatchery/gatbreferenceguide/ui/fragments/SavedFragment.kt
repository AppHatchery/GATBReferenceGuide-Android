package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentSavedBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.data.ViewPagerData
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.*
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASavedViewModel
import org.apphatchery.gatbreferenceguide.utils.getBottomNavigationView
import org.apphatchery.gatbreferenceguide.utils.setupToolbar
import org.apphatchery.gatbreferenceguide.utils.snackBar
import org.apphatchery.gatbreferenceguide.utils.toggleVisibility

@AndroidEntryPoint
class SavedFragment : BaseFragment(R.layout.fragment_saved) {

    private lateinit var bind: FragmentSavedBinding
    private lateinit var faSavedViewPagerAdapter: FASavedViewPagerAdapter
    private lateinit var faSavedBookmarkAdapter: FASavedBookmarkAdapter
    private lateinit var faSavedNoteAdapter: FANoteAdapter
    private lateinit var faSavedRecentAdapter: FASavedRecentAdapter

    private val viewPagerHeadingTitle = arrayListOf("Recent", "Bookmarks", "Notes")
    private val viewModel: FASavedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        bind = FragmentSavedBinding.bind(view)
        bind.toolbar.setupToolbar(requireActivity(), "Saved", null, false)

        faSavedRecentAdapter = FASavedRecentAdapter().apply {
            viewModel.getRecentEntity.observe(viewLifecycleOwner) {
                submitList(it)
            }
            itemClickCallback {
                viewModel.getSubChapterInfo(it.subChapterId.toString())
                    .observe(viewLifecycleOwner) { subChapterEntity ->
                        viewModel.getChapterInfo(subChapterEntity.chapterId)
                            .observe(viewLifecycleOwner) { chapterEntity ->
                                SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                                    BodyUrl(chapterEntity, subChapterEntity), null
                                ).apply {
                                    findNavController().navigate(this)
                                }
                            }
                    }
            }
        }


        faSavedNoteAdapter = FANoteAdapter(View.VISIBLE).apply {
            viewModel.getNoteEntity.observe(viewLifecycleOwner) {
                submitList(it)
            }

            itemClickCallback {
                viewModel.getSubChapterInfo(it.subChapterId.toString())
                    .observe(viewLifecycleOwner) { subChapter ->
                        viewModel.getChapterInfo(subChapter.chapterId)
                            .observe(viewLifecycleOwner) { chapter ->

                                val chartEntity = ChartEntity(
                                    it.noteId, it.noteTitle, subChapter.subChapterTitle, 0
                                )

                                val chartAndSubChapter = ChartAndSubChapter(chartEntity, subChapter)
                                SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                                    BodyUrl(chapter, subChapter),
                                    if (it.noteId.contains("table_")) chartAndSubChapter else null
                                ).apply {
                                    findNavController().navigate(this)
                                }
                            }
                    }
            }
        }


        faSavedBookmarkAdapter = FASavedBookmarkAdapter().apply {
            viewModel.getBookmarkEntity.observe(viewLifecycleOwner) {
                submitList(it)
            }

            itemClickCallback {
                viewModel.getSubChapterInfo(it.bookmarkId)
                    .observe(viewLifecycleOwner) { subChapterEntity ->
                        viewModel.getChapterInfo(subChapterEntity.chapterId)
                            .observe(viewLifecycleOwner) { chapterEntity ->
                                SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                                    BodyUrl(
                                        chapterEntity,
                                        subChapterEntity
                                    ), null
                                ).apply {
                                    findNavController().navigate(this)
                                }
                            }
                    }
            }

        }

        val bookmarkSwipeHandler = object : SwipeDecoratorCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val bookmark = faSavedBookmarkAdapter.currentList[position]
                viewModel.deleteBookmark(bookmark)
                bind.root.snackBar(" Bookmark deleted.").also {
                    it.setAction("undo") {
                        viewModel.insertBookmark(bookmark)
                    }
                }
            }
        }


        val noteSwipeHandler = object : SwipeDecoratorCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = faSavedNoteAdapter.currentList[position]
                viewModel.deleteNote(note)
                bind.root.snackBar(" Note deleted.").also {
                    it.setAction("undo") {
                        viewModel.insertNote(note)
                    }
                }
            }
        }

        val viewPagerAdapter = arrayListOf(
            ViewPagerData(faSavedRecentAdapter),
            ViewPagerData(faSavedBookmarkAdapter, bookmarkSwipeHandler),
            ViewPagerData(faSavedNoteAdapter, noteSwipeHandler),
        )


        faSavedViewPagerAdapter = FASavedViewPagerAdapter().apply {
            submitList(viewPagerAdapter)
            bind.viewPager.adapter = this
        }

        TabLayoutMediator(bind.tabLayout, bind.viewPager) { tab, position ->
            tab.text = viewPagerHeadingTitle[position]
        }.attach()


    }
}