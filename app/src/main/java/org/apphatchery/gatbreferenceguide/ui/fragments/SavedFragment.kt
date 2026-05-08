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
import org.apphatchery.gatbreferenceguide.utils.LegacyRedirects
import org.apphatchery.gatbreferenceguide.utils.navigateSafe
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
                val original = it
                val redirectedBookmarkId = LegacyRedirects.redirectBookmarkId(original.bookmarkId)
                val redirectedSubChapter = LegacyRedirects.redirectSubChapterKey(original.subChapter)

                val didRedirectBookmarkId = redirectedBookmarkId != original.bookmarkId
                val didRedirectSubChapter = redirectedSubChapter != original.subChapter
                val didRedirect = didRedirectBookmarkId || didRedirectSubChapter

                if (didRedirect) {
                    val message = if (isTableId(original.bookmarkId) || isTableId(redirectedBookmarkId)) {
                        "This table has changed in the latest update. Redirecting to the updated table…"
                    } else {
                        "This bookmark has changed in the latest update. Redirecting to the updated section…"
                    }
                    bind.root.snackBar(message)
                }

                if (isTableId(original.bookmarkId) || isTableId(redirectedBookmarkId)) {
                    openChartBookmark(
                        original = original,
                        redirectedBookmarkId = redirectedBookmarkId,
                        redirectedSubChapter = redirectedSubChapter,
                        didRedirect = didRedirect,
                    )
                } else {
                    openSubChapterBookmark(
                        original = original,
                        redirectedBookmarkId = redirectedBookmarkId,
                        redirectedSubChapter = redirectedSubChapter,
                        didRedirect = didRedirect,
                    )
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
        viewModel.getChapterInfoOrNull(subChapterEntity.chapterId)
            .observe(viewLifecycleOwner) { chapterEntity ->
                if (chapterEntity == null) {
                    bind.root.snackBar("This bookmark points to content that was removed or renamed.")
                    return@observe
                }

                findNavController().navigateSafe(
                    SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                        BodyUrl(chapterEntity, subChapterEntity, ""), null
                    )
                )
            }
    }

    private fun setSavedData(savedType: SavedType, itemCount: Int) =
        viewModel.setSavedItemCount(SavedTypeData(savedType, itemCount))


    private fun isTableId(id: String): Boolean = id.startsWith("table_")

    private fun shouldOverwriteTitleOnRedirect(bookmark: BookmarkEntity): Boolean {
        // Default titles are saved as subchapter title (same as subChapter field).
        // Preserve user-custom titles during redirects.
        return bookmark.bookmarkTitle.isBlank() || bookmark.bookmarkTitle == bookmark.subChapter
    }


    private fun openSubChapterBookmark(
        original: BookmarkEntity,
        redirectedBookmarkId: String,
        redirectedSubChapter: String,
        didRedirect: Boolean,
    ) {
        var handled = false
        viewModel.getSubChapterInfoOrNull(redirectedBookmarkId)
            .observe(viewLifecycleOwner) { subChapterEntity ->
                if (handled) return@observe

                if (subChapterEntity != null) {
                    handled = true
                    if (didRedirect) {
                        val newTitle = if (shouldOverwriteTitleOnRedirect(original)) {
                            subChapterEntity.subChapterTitle
                        } else {
                            original.bookmarkTitle
                        }
                        val newSubChapter = subChapterEntity.subChapterTitle
                        viewModel.repairRedirectedBookmark(
                            oldId = original.bookmarkId,
                            newId = redirectedBookmarkId,
                            newTitle = newTitle,
                            newSubChapter = newSubChapter,
                        )
                    }
                    actionSavedFragmentToBodyFragment(subChapterEntity)
                    return@observe
                }

                // Fallback: older bookmarks sometimes store title in subChapter field.
                viewModel.getSubChapterInfoOrNull(redirectedSubChapter)
                    .observe(viewLifecycleOwner) { fallback ->
                        if (handled) return@observe
                        handled = true
                        if (fallback != null) {
                            if (didRedirect) {
                                val newTitle = if (shouldOverwriteTitleOnRedirect(original)) {
                                    fallback.subChapterTitle
                                } else {
                                    original.bookmarkTitle
                                }
                                viewModel.repairRedirectedBookmark(
                                    oldId = original.bookmarkId,
                                    newId = redirectedBookmarkId,
                                    newTitle = newTitle,
                                    newSubChapter = fallback.subChapterTitle,
                                )
                            }
                            actionSavedFragmentToBodyFragment(fallback)
                        } else {
                            bind.root.snackBar("This bookmark points to content that was removed or renamed.")
                        }
                    }
            }
    }


    private fun openChartBookmark(
        original: BookmarkEntity,
        redirectedBookmarkId: String,
        redirectedSubChapter: String,
        didRedirect: Boolean,
    ) {
        var handled = false
        viewModel.getChartAndSubChapterByIdOrNull(redirectedBookmarkId)
            .observe(viewLifecycleOwner) { chartAndSubChapter ->
                if (handled) return@observe

                if (chartAndSubChapter == null) {
                    // Chart/table no longer exists. Fall back to opening the bookmarked subchapter.
                    viewModel.getSubChapterInfoOrNull(redirectedSubChapter)
                        .observe(viewLifecycleOwner) { fallbackSubChapter ->
                            if (handled) return@observe
                            handled = true

                            if (fallbackSubChapter != null) {
                                bind.root.snackBar(
                                    "This table was removed or merged in the latest update. Opening the related section instead."
                                )
                                actionSavedFragmentToBodyFragment(fallbackSubChapter)
                            } else {
                                bind.root.snackBar("This table bookmark points to content that was removed or renamed.")
                            }
                        }
                    return@observe
                }

                handled = true

                if (didRedirect) {
                    val newTitle = if (shouldOverwriteTitleOnRedirect(original)) {
                        chartAndSubChapter.chartEntity.chartTitle
                    } else {
                        original.bookmarkTitle
                    }
                    val newSubChapter = chartAndSubChapter.subChapterEntity.subChapterTitle
                    viewModel.repairRedirectedBookmark(
                        oldId = original.bookmarkId,
                        newId = redirectedBookmarkId,
                        newTitle = newTitle,
                        newSubChapter = newSubChapter,
                    )
                }

                viewModel.getChapterInfoOrNull(chartAndSubChapter.subChapterEntity.chapterId)
                    .observe(viewLifecycleOwner) { chapterEntity ->
                        if (chapterEntity == null) {
                            bind.root.snackBar("This table bookmark points to content that was removed or renamed.")
                            return@observe
                        }

                        findNavController().navigateSafe(
                            SavedFragmentDirections.actionSavedFragmentToBodyFragment(
                                BodyUrl(chapterEntity, chartAndSubChapter.subChapterEntity, ""),
                                chartAndSubChapter
                            )
                        )
                    }
            }
    }


    // navigateToChart() replaced by openChartBookmark(), which also repairs redirected rows.
        
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
            // Temporarily hide edit dialog to avoid stacking
            dialog.hide()

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

            // Color entire title line 
            val titleLine = bookmark.bookmarkTitle + "\u200B"
            val baseText = "Delete Bookmark?\n$titleLine"
            val start = baseText.indexOf('\n') + 1
            val end = baseText.length
            val spannable = android.text.SpannableString(baseText)
            val color = ContextCompat.getColor(requireContext(), R.color.reddish)
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(color),
                start,
                end,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            messageTextView.text = spannable

            cancelButton.setOnClickListener {
                confirmDialog.dismiss()
                // Restore the edit dialog on cancel
                dialog.show()
            }

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
            val newTitle = bookmarkNameEdit.text.toString().trim()
            if (newTitle.isEmpty()) {
                bookmarkNameEdit.error = getString(R.string.bookmark_title_required)
                return@setOnClickListener
            }
            val updatedBookmark = bookmark.copy(
                bookmarkTitle = newTitle
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