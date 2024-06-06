package org.apphatchery.gatbreferenceguide.ui.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentBookmarksBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FASavedBookmarkAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.BookmarksViewModel

class BookmarksFragment : BaseFragment(R.layout.fragment_bookmarks) {

    private lateinit var bind: FragmentBookmarksBinding
    private lateinit var faSavedBookmarkAdapter: FASavedBookmarkAdapter

    companion object {
        fun newInstance() = BookmarksFragment()
    }

    private val viewModel: BookmarksViewModel by viewModels()

    enum class SavedType {
        BOOKMARK
    }

    data class SavedTypeData(
        val savedType: SavedType = SavedType.BOOKMARK,
        val itemCount: Int = -1,
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentBookmarksBinding.bind(view)

        super.onCreate(savedInstanceState)
    }

    private fun actionBookmarksFragmentToBodyFragment(subChapterEntity: SubChapterEntity) {
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

    private fun setSavedData(savedType: SavedFragment.SavedType, itemCount: Int) =
        viewModel.setSavedItemCount(SavedFragment.SavedTypeData(savedType, itemCount))


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }
}