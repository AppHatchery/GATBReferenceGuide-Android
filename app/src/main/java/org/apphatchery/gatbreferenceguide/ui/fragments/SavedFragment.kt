package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentSavedBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.data.ViewPagerData
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.*
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASavedViewModel
import org.apphatchery.gatbreferenceguide.utils.snackBar

@AndroidEntryPoint
class SavedFragment : BaseFragment(R.layout.fragment_saved) {

    private lateinit var bind: FragmentSavedBinding
    private lateinit var faSavedViewPagerAdapter: FASavedViewPagerAdapter
    private lateinit var faSavedBookmarkAdapter: FASavedBookmarkAdapter
    private lateinit var faSavedNoteAdapter: FANoteAdapter
    private lateinit var faSavedRecentAdapter: FASavedRecentAdapter

    private val viewPagerHeadingTitle = arrayListOf("Bookmarks", "Notes", "Recent")
    private val viewModel: FASavedViewModel by viewModels()
    private var onViewCreated = true


    enum class SavedType {
        BOOKMARK, NOTES, RECENT
    }


    data class SavedTypeData(
        val savedType: SavedType = SavedType.BOOKMARK,
        val itemCount: Int = -1,
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        bind = FragmentSavedBinding.bind(view)

        faSavedRecentAdapter = FASavedRecentAdapter().apply {
            viewModel.getRecentEntity.observe(viewLifecycleOwner) {
                submitList(if (it.size > 10) it.subList(0, 10) else it)
            }
            itemClickCallback {
                if (it.id.contains("table_")) it.id.navigateToChart() else {
                    viewModel.getSubChapterInfo(it.id)
                        .observe(viewLifecycleOwner) { subChapterEntity ->
                            viewModel.getChapterInfo(subChapterEntity.chapterId)
                                .observe(viewLifecycleOwner) { chapterEntity ->
                                    SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                                        BodyUrl(chapterEntity, subChapterEntity, ""), null
                                    ).apply {
                                        findNavController().navigate(this)
                                    }
                                }
                        }
                }
            }
        }

        bind.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                Log.e("TAG", "onPageSelected: "+ position )
                when (position) {
                    2 -> setSavedData(
                        SavedType.RECENT, faSavedRecentAdapter.currentList.size
                    )

                    0 -> setSavedData(
                        SavedType.BOOKMARK, faSavedBookmarkAdapter.currentList.size
                    )

                    1 -> setSavedData(
                        SavedType.NOTES,
                        faSavedNoteAdapter.currentList.size
                    )
                }
            }
        })



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
                                    it.noteId,
                                    it.noteTitle,
                                    subChapter.subChapterTitle,
                                    subChapter.subChapterId,
                                    0
                                )

                                val chartAndSubChapter = ChartAndSubChapter(chartEntity, subChapter)
                                SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                                    BodyUrl(chapter, subChapter, ""),
                                    if (it.noteId.contains("table_")) chartAndSubChapter else null
                                ).apply {
                                    findNavController().navigate(this)
                                }
                            }
                    }
                setSavedData(SavedType.NOTES, currentList.size)
            }
        }


        faSavedBookmarkAdapter = FASavedBookmarkAdapter().apply {
            viewModel.getBookmarkEntity.observe(viewLifecycleOwner) {
                submitList(it)
                if (onViewCreated) viewModel.setSavedItemCount(SavedTypeData(itemCount = it.size))
                onViewCreated = false
            }

            itemClickCallback {
                if (it.bookmarkId.contains("table_")) it.bookmarkId.navigateToChart()
                else {
                    viewModel.getSubChapterInfo(it.bookmarkId)
                        .observe(viewLifecycleOwner) { subChapterEntity ->
                            if (subChapterEntity == null) {
                                viewModel.getSubChapterInfo(it.subChapter)
                                    .observe(viewLifecycleOwner) {
                                        actionSavedFragmentToBodyFragment(it)
                                    }
                            } else
                                actionSavedFragmentToBodyFragment(subChapterEntity)
                        }
                }
            }

        }

        val bookmarkSwipeHandler = object : SwipeDecoratorCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val bookmark = faSavedBookmarkAdapter.currentList[position]
                val currentListSize = faSavedBookmarkAdapter.currentList.size
                viewModel.deleteBookmark(bookmark)
                setSavedData(SavedType.BOOKMARK, currentListSize.minus(1))
                bind.root.snackBar(getString(R.string.bookmark_deleted, bookmark.bookmarkTitle))
                    .also {
                        it.setAction(getString(R.string.undo)) {
                            viewModel.insertBookmark(bookmark)
                            setSavedData(SavedType.BOOKMARK, currentListSize)
                        }
                    }
            }
        }


        val noteSwipeHandler = object : SwipeDecoratorCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = faSavedNoteAdapter.currentList[position]
                viewModel.deleteNote(note)
                val currentListSize = faSavedNoteAdapter.currentList.size
                setSavedData(SavedType.NOTES, currentListSize.minus(1))
                bind.root.snackBar(getString(R.string.note_deleted)).also {
                    it.setAction(getString(R.string.undo)) {
                        viewModel.insertNote(note)
                        setSavedData(SavedType.NOTES, currentListSize)
                    }
                }
            }
        }

        val viewPagerAdapter = arrayListOf(
            ViewPagerData(faSavedBookmarkAdapter, bookmarkSwipeHandler),
            ViewPagerData(faSavedNoteAdapter, noteSwipeHandler),
            ViewPagerData(faSavedRecentAdapter),
        )


        faSavedViewPagerAdapter = FASavedViewPagerAdapter(viewModel, viewLifecycleOwner).apply {
            submitList(viewPagerAdapter)
            bind.viewPager.adapter = this
        }

        TabLayoutMediator(bind.tabLayout, bind.viewPager) { tab, position ->
            tab.text = viewPagerHeadingTitle[position]
        }.attach()


    }

    private fun actionSavedFragmentToBodyFragment(subChapterEntity: SubChapterEntity) {
        viewModel.getChapterInfo(subChapterEntity.chapterId)
            .observe(viewLifecycleOwner) { chapterEntity ->
                SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                    BodyUrl(
                        chapterEntity,
                        subChapterEntity,
                        ""
                    ), null
                ).apply {
                    findNavController().navigate(this)
                }
            }
    }

    private fun setSavedData(savedType: SavedType, itemCount: Int) =
        viewModel.setSavedItemCount(SavedTypeData(savedType, itemCount))


    private fun String.navigateToChart() = viewModel.getChartAndSubChapterById(this)
        .observe(viewLifecycleOwner) {
            viewModel.getChapterInfo(it.subChapterEntity.chapterId)
                .observe(viewLifecycleOwner) { chapterEntity ->
                    SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                        BodyUrl(chapterEntity, it.subChapterEntity, ""),
                        it
                    ).apply {
                        findNavController().navigate(this)
                    }
                }
        }

}