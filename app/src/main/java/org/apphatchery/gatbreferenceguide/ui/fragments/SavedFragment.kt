package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentSavedBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.*
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASavedViewModel
import org.apphatchery.gatbreferenceguide.utils.snackBar

@AndroidEntryPoint
class SavedFragment : BaseFragment(R.layout.fragment_saved) {

    private lateinit var bind: FragmentSavedBinding
    private lateinit var faSavedBookmarkAdapter: FASavedBookmarkAdapter
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

        // Initialize bookmark adapter
        faSavedBookmarkAdapter = FASavedBookmarkAdapter().apply {
            viewModel.getBookmarkEntity.observe(viewLifecycleOwner) { bookmarks ->
                submitList(bookmarks)
                
                // Show/hide views based on bookmark list size
                val noBookmarksLayout = view.findViewById<View>(R.id.no_bookmarks_layout)
                if (bookmarks.isEmpty()) {
                    bind.bookmarksRecyclerView.visibility = android.view.View.GONE
                    noBookmarksLayout.visibility = android.view.View.VISIBLE
                } else {
                    bind.bookmarksRecyclerView.visibility = android.view.View.VISIBLE
                    noBookmarksLayout.visibility = android.view.View.GONE
                }
                
                if (onViewCreated) viewModel.setSavedItemCount(SavedTypeData(itemCount = bookmarks.size))
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
        
        // Setup edit icon callback
        faSavedBookmarkAdapter.itemEditCallback {
            showEditBookmarkDialog(it)
        }

        // Setup RecyclerView for bookmarks
        bind.bookmarksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = faSavedBookmarkAdapter
        }
        setSavedData(SavedType.BOOKMARK, faSavedBookmarkAdapter.currentList.size)

        val bookmarkSwipeHandler = object : SwipeDecoratorCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val bookmark = faSavedBookmarkAdapter.currentList[position]
                val currentListSize = faSavedBookmarkAdapter.currentList.size
                viewModel.deleteBookmark(bookmark)
                setSavedData(SavedType.BOOKMARK, currentListSize.minus(1))
                showDeleteDeletionCard(bookmark.bookmarkTitle)
            }
        }




        // Setup swipe-to-delete for bookmarks
        val itemTouchHelper = ItemTouchHelper(bookmarkSwipeHandler)
        itemTouchHelper.attachToRecyclerView(bind.bookmarksRecyclerView)


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
        
    private fun showEditBookmarkDialog(bookmark: BookmarkEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_bookmark, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        // Make dialog background transparent so custom background shows
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Get dialog views
        val bookmarkNameEdit = dialogView.findViewById<android.widget.EditText>(R.id.bookmark_name_edit)
        val sourceNameText = dialogView.findViewById<android.widget.TextView>(R.id.bookmark_source_name)
        val sourceFullText = dialogView.findViewById<android.widget.TextView>(R.id.bookmark_source_full)
        val closeButton = dialogView.findViewById<android.widget.ImageView>(R.id.close_dialog)
        val deleteButton = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.delete_bookmark_button)
        val saveButton = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.save_changes_button)
        
        // Populate fields with current bookmark data
        bookmarkNameEdit.setText(bookmark.bookmarkTitle)
        sourceNameText.text = bookmark.bookmarkTitle
        sourceFullText.text = bookmark.subChapter
        
        // Set up click listeners
        closeButton.setOnClickListener { dialog.dismiss() }
        
        deleteButton.setOnClickListener {
            // Show dedicated bookmark confirmation dialog
            val confirmView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bookmark_deletion_confirmation, null)
            val confirmDialog = android.app.AlertDialog.Builder(requireContext())
                .setView(confirmView)
                .create()

            // Transparent background so custom background shows
            confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val messageTextView = confirmView.findViewById<android.widget.TextView>(R.id.deletionMessage)
            val cancelButton = confirmView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.cancelButton)
            val deleteButtonConfirm = confirmView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.confirmDeleteButton)

            // Build: "Delete Bookmark?\n<title>" and color only <title> with @color/reddish
            val baseText = "Delete Bookmark?\n${bookmark.bookmarkTitle}"
            val spannable = android.text.SpannableString(baseText)
            val prefix = "Delete Bookmark\n"
            val start = prefix.length
            val end = start + bookmark.bookmarkTitle.length
            val color = ContextCompat.getColor(requireContext(), R.color.reddish)
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(color),
                start,
                end,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            messageTextView.text = spannable

            cancelButton.setOnClickListener { confirmDialog.dismiss() }

            deleteButtonConfirm.setOnClickListener {
                viewModel.deleteBookmark(bookmark)
                // Close both dialogs
                confirmDialog.dismiss()
                dialog.dismiss()

                showDeleteDeletionCard(bookmark.bookmarkTitle)
            }

            confirmDialog.show()
        }
        
        saveButton.setOnClickListener {
            val updatedBookmark = bookmark.copy(
                bookmarkTitle = bookmarkNameEdit.text.toString()
            )
            viewModel.updateBookmark(updatedBookmark)
            dialog.dismiss()
            showNotificationCard(message = getString(R.string.bookmark_updated))
        }
        
        dialog.show()
        }

    private fun showNotificationCard(message: String) {
        val container = view?.findViewById<android.widget.RelativeLayout>(R.id.delete_card_container)
        val textView = view?.findViewById<android.widget.TextView>(R.id.delete_card_text)
        val color = ContextCompat.getColor(requireContext(), R.color.reddish)
        if (container != null && textView != null) {
            textView.text = message
            container.visibility = android.view.View.VISIBLE
            container.postDelayed({
                container.visibility = android.view.View.GONE
            }, 3000)
        }
    }

    private fun showDeleteDeletionCard(bookmarkTitle: String) {
        val container = view?.findViewById<android.widget.RelativeLayout>(R.id.delete_card_container)
        val textView = view?.findViewById<android.widget.TextView>(R.id.delete_card_text)
        if (container != null && textView != null) {
            val fullText = "Bookmark deleted\n$bookmarkTitle"
            val spannable = android.text.SpannableString(fullText)
            val start = "Bookmark deleted\n".length
            val end = start + bookmarkTitle.length
            val color = ContextCompat.getColor(requireContext(), R.color.reddish)
            spannable.setSpan(android.text.style.ForegroundColorSpan(color), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannable
            container.visibility = android.view.View.VISIBLE
            container.postDelayed({
                container.visibility = android.view.View.GONE
            }, 3000)
        }
    }

}