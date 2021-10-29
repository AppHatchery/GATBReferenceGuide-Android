package org.apphatchery.gatbreferenceguide.ui.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentBodyBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.*
import org.apphatchery.gatbreferenceguide.enums.BookmarkType
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FANoteAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FANoteColorAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeDecoratorCallback
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FABodyViewModel
import org.apphatchery.gatbreferenceguide.utils.*

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class BodyFragment : BaseFragment(R.layout.fragment_body) {

    private lateinit var bind: FragmentBodyBinding
    private val bodyFragmentArgs: BodyFragmentArgs by navArgs()
    private lateinit var bodyUrl: BodyUrl
    private val viewModel: FABodyViewModel by viewModels()
    private var bookmarkEntity = BookmarkEntity()
    private lateinit var faNoteColorAdapter: FANoteColorAdapter
    private lateinit var faNoteAdapter: FANoteAdapter
    private var chartAndSubChapter: ChartAndSubChapter? = null
    private var bookmarkType: BookmarkType = BookmarkType.SUBCHAPTER
    private lateinit var subChapterEntity: SubChapterEntity
    private lateinit var chapterEntity: ChapterEntity
    private var baseURL = ""
    private var isCollapsed = false

    private fun setupBookmark(id: String) {
        viewModel.getBookmarkById(id).observe(viewLifecycleOwner) {
            if (it != null) {
                bookmarkEntity = it
                bind.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_star)
            } else {
                bind.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_star_outline)
            }
        }
    }


    private fun onDeleteNoteSnackbar(note: NoteEntity) =
        bind.root.snackBar(" Note deleted.").also {
            it.setAction("undo") {
                viewModel.insertNote(note)
            }
        }

    private fun View.changeBackgroundColor(
        @ColorRes color: Int
    ) = background.setTint(ContextCompat.getColor(context, color))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentBodyBinding.bind(view)
        bodyUrl = bodyFragmentArgs.bodyUrl
        setHasOptionsMenu(true)

        baseURL = "file://" + requireContext().cacheDir.toString() + "/"

        chartAndSubChapter = bodyFragmentArgs.chartAndSubChapter
        subChapterEntity = bodyUrl.subChapterEntity
        chapterEntity = bodyUrl.chapterEntity


        viewModel.recentOpen(
            RecentEntity(
                subChapterEntity.subChapterId,
                subChapterEntity.subChapterTitle
            )
        )


        faNoteColorAdapter = FANoteColorAdapter(requireContext()).also {
            it.submitList(NOTE_COLOR)
        }


        val swipeHandler = object : SwipeDecoratorCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = faNoteAdapter.currentList[position]
                viewModel.deleteNote(note)
                onDeleteNoteSnackbar(note)
            }
        }



        ItemTouchHelper(swipeHandler).also {
            it.attachToRecyclerView(bind.recyclerviewNote)
        }



        setupWebView()
        bind.apply {

            toolbar.enableToolbar(requireActivity())
            toolbarBackButton.setOnClickListener { requireActivity().onBackPressed() }
            bookmarkImageButton.setOnClickListener { onBookmarkListener() }

            if (chartAndSubChapter != null) isChartView() else {
                toolbarTitle.text = chapterEntity.chapterTitle
                textviewSubChapter.text = subChapterEntity.subChapterTitle
                bodyWebView.loadUrl(baseURL + PAGES_DIR + subChapterEntity.url + EXTENSION)
            }

            addNote.setOnClickListener { onNoteListener() }

            faNoteAdapter = FANoteAdapter().also {
                viewModel.getNote(
                    if (bookmarkType == BookmarkType.CHART) chartAndSubChapter!!.chartEntity.id else subChapterEntity.subChapterId.toString()
                ).observe(viewLifecycleOwner) { data ->
                    it.submitList(data)
                    showNoteCollapseControl(data.isEmpty())
                    bind.noteCountTextView.text = getString(R.string.notes_count, data.size)
                }

                it.itemClickCallback { onNoteListenerEdit(it) }
            }


            recyclerviewNote.apply {
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
                layoutManager = GridLayoutManager(requireContext(), 1)
                adapter = faNoteAdapter
            }

            shareButton.setOnClickListener {
                Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, subChapterEntity.subChapterTitle)
                    .setType("text/plain")
                    .also { requireActivity().startActivity(Intent.createChooser(it, "Share")) }
            }

            collapseActionButton.setOnClickListener {
                bind.recyclerviewNote.apply {
                    if (isCollapsed.not()) {
                        scaleAnimate(this, 0f) {
                            (it as ImageView).setImageResource(R.drawable.ic_baseline_arrow_down)
                            visibility = View.GONE
                        }
                    } else {
                        visibility = View.VISIBLE
                        scaleAnimate(this, 1f) {
                            (it as ImageView).setImageResource(R.drawable.ic_baseline_arrow_up)
                        }
                    }
                    isCollapsed = !isCollapsed
                }
            }
        }


        setupBookmark(
            if (bookmarkType == BookmarkType.CHART)
                chartAndSubChapter!!.chartEntity.id else
                subChapterEntity.subChapterId.toString()
        )
    }

    private fun scaleAnimate(view: View, scaleFactor: Float, onAnimationCompleted: () -> Unit) =
        view.animate().apply {
            scaleX(scaleFactor).scaleY(scaleFactor)
            duration = 200
            setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) = Unit
                override fun onAnimationEnd(animation: Animator?) = onAnimationCompleted()
                override fun onAnimationCancel(animation: Animator?) = Unit
                override fun onAnimationRepeat(animation: Animator?) = Unit
            })
        }

    private fun showNoteCollapseControl(isEmpty: Boolean) {
        bind.collapsableNoteRoot.visibility =
            if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun isChartView() = bind.apply {
        viewModel.getChapterById(chartAndSubChapter!!.subChapterEntity.chapterId)
            .observeOnce(viewLifecycleOwner) { chapterEntity ->
                toolbarTitle.text = chapterEntity.chapterTitle


//                tableViewLinearLayoutCompat.visibility = View.VISIBLE
                tableName.apply {
                    text = chartAndSubChapter!!.subChapterEntity.subChapterTitle
                    setOnClickListener {
                        val directions =
                            BodyFragmentDirections.actionBodyFragmentSelf(
                                bodyUrl.copy(
                                    chapterEntity = ChapterEntity(chapterTitle = chapterEntity.chapterTitle)
                                ), null
                            )
                        findNavController().navigate(directions)
                    }
                }
            }

        textviewSubChapter.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.ic_baseline_bar_chart,
            0,
            0,
            0
        )


        bookmarkType = BookmarkType.CHART
        textviewSubChapter.text = chartAndSubChapter!!.chartEntity.chartTitle

        val loadUrl = baseURL + PAGES_DIR + chartAndSubChapter!!.chartEntity.id + EXTENSION
        bodyWebView.loadUrl(loadUrl)

    }

    private fun onNoteListenerEdit(note: NoteEntity) = Dialog(requireContext()).dialog().apply {
        setContentView(R.layout.dialog_note)
        val deleteButton = findViewById<Button>(R.id.noteCancelButton)
        val updateButton = findViewById<Button>(R.id.noteSaveButton)
        val noteBody = findViewById<AppCompatEditText>(R.id.noteBody)
        val noteTitle = findViewById<TextView>(R.id.noteTitle)
        val noteColorRecyclerView = findViewById<RecyclerView>(R.id.noteRecyclerViewColor)
        noteColorRecyclerView.apply {
            faNoteColorAdapter.selectedColor = note.noteColor
            adapter = faNoteColorAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        findViewById<View>(R.id.closeDialog).setOnClickListener { dismiss() }
        noteTitle.text = "Edit Note"
        noteBody.apply {
            setText(note.noteText)
            setSelection(note.noteText.length)
            requestFocus()
        }

        deleteButton.apply {
            text = "Delete"
            setTextColor(Color.WHITE)
             setOnClickListener {
                requireContext().alertDialog(
                    message = "Are you sure you want to delete this note ?"
                ) {
                    dismiss()
                    viewModel.deleteNote(note)
                    onDeleteNoteSnackbar(note)
                }
            }
        }


        updateButton.apply {
            text = "Update"
            setTextColor(Color.WHITE)
             setOnClickListener {
                if (noteBody.text.toString().trim()
                        .isEmpty()
                ) bind.root.snackBar("Please enter notes to update.") else {
                    viewModel.updateNote(
                        note.copy(
                            noteText = noteBody.text.toString().trim(),
                            lastEdit = System.currentTimeMillis(),
                            noteColor = faNoteColorAdapter.selectedColor,
                        )
                    )
                    dismiss()
                    requireContext().toast("Note has been updated.")
                }
            }
        }
        safeDialogShow()
    }


    private fun onNoteListener() = Dialog(requireContext()).dialog().apply {
        setContentView(R.layout.dialog_note)
        val cancelButton = findViewById<View>(R.id.noteCancelButton)
        val saveButton = findViewById<View>(R.id.noteSaveButton)
        val noteBody = findViewById<AppCompatEditText>(R.id.noteBody)
        val noteColorRecyclerView = findViewById<RecyclerView>(R.id.noteRecyclerViewColor)
        findViewById<View>(R.id.closeDialog).setOnClickListener { dismiss() }
        noteColorRecyclerView.apply {
            adapter = faNoteColorAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        cancelButton.setOnClickListener { dismiss() }
        saveButton.setOnClickListener {
            onSaveNote(noteBody.text.toString().trim())
            dismiss()
        }
        safeDialogShow()
    }

    private fun onBookmarkListener() {

        Dialog(requireContext()).dialog().apply {
            setContentView(R.layout.dialog_bookmark)
            val cancelButton = findViewById<Button>(R.id.bookmarkCancelButton)
            val saveButton = findViewById<Button>(R.id.bookmarkSaveButton)
            val bookTitleTextInputEditText =
                findViewById<AppCompatEditText>(R.id.bookmarkTitleTextInputEditText)
            bookTitleTextInputEditText.also {
                it.setText(subChapterEntity.subChapterTitle)
                it.requestFocus()
            }

            cancelButton.setOnClickListener { dismiss() }

            saveButton.setOnClickListener {
                onSaveBookmark(bookTitleTextInputEditText.text.toString().trim())
                dismiss()
            }


            findViewById<View>(R.id.close_dialog).setOnClickListener { dismiss() }


            if (bookmarkEntity.bookmarkId != "0") {

                findViewById<TextView>(R.id.bookmarkTitle).text ="Edit bookmark"
                bookTitleTextInputEditText.also {
                    it.setText(bookmarkEntity.bookmarkTitle)
                    it.setSelection(bookmarkEntity.bookmarkTitle.length)
                    it.requestFocus()
                }

                cancelButton.apply {
                    text = "Delete"
                 }


                saveButton.apply {
                    text = "Update"
                 }

                cancelButton.setOnClickListener {
                    requireContext().alertDialog(
                        message = "Are you sure you want to remove " + bookmarkEntity.bookmarkTitle + "  from you bookmarks ?"
                    ) {
                        dismiss()
                        viewModel.deleteBookmark(bookmarkEntity)
                        requireContext().toast(bookmarkEntity.bookmarkTitle + " has been removed from your bookmarks.")
                        bookmarkEntity = BookmarkEntity()
                    }
                }
                saveButton.setOnClickListener {
                    bookmarkEntity.copy(
                        bookmarkTitle = bookTitleTextInputEditText.text.toString().trim()
                    ).also {
                        dismiss()
                        viewModel.updateBookmark(it)
                        requireContext().toast("Bookmark has been updated.")
                    }
                }
            }

            safeDialogShow()
        }

    }

    private fun onSaveBookmark(text: String) {
        viewModel.insertBookmark(
            BookmarkEntity(
                bookmarkTitle = if (text.isEmpty()) subChapterEntity.subChapterTitle else text,
                bookmarkId = if (bookmarkType == BookmarkType.CHART) chartAndSubChapter!!.chartEntity.id else subChapterEntity.subChapterId.toString()
            )
        )
        requireContext().toast("Bookmark saved")
    }

    private fun onSaveNote(noteBody: String) = bind.root.apply {
        if (noteBody.isBlank()) snackBar("Please enter notes to save.") else {
            viewModel.insertNote(
                NoteEntity(
                    noteId = if (bookmarkType == BookmarkType.CHART) chartAndSubChapter!!.chartEntity.id else subChapterEntity.subChapterId.toString(),
                    noteTitle = if (bookmarkType == BookmarkType.CHART) chartAndSubChapter!!.chartEntity.chartTitle else subChapterEntity.subChapterTitle,
                    subChapterId = subChapterEntity.subChapterId,
                    noteColor = faNoteColorAdapter.selectedColor,
                    noteText = noteBody
                )
            )
            snackBar("Note saved")
        }
    }

    private fun setupWebView() = bind.bodyWebView.apply {
        with(settings) {
            allowContentAccess = true
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_NO_CACHE
        }


        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val link = request?.url.toString()
                return when {
                    link.subSequence(0, 4).toString().lowercase() == "http".lowercase() -> {

                        requireActivity().apply {
                            alertDialog("", message = "Open link in browser ?") {
                                startActivity(
                                    Intent(Intent.ACTION_VIEW)
                                        .setData(Uri.parse(link))
                                )
                            }
                        }
                        true
                    }
                    link.contains("#") -> {
                        bookmarkType = BookmarkType.SUBCHAPTER
                        BodyFragmentDirections.actionBodyFragmentSelf(
                            bodyUrl.copy(
                                chapterEntity = ChapterEntity(chapterTitle = chapterEntity.chapterTitle)
                            ), null
                        ).also { findNavController().navigate(it) }
                        true
                    }
                    else -> {
                        val stripLink = link.substring(link.lastIndexOf("/") + 1, link.length)
                        stripLink.replace(EXTENSION, "")
                        gotoNavController(stripLink.replace(EXTENSION, ""))
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }

            }
        }
    }


    private fun gotoNavController(url: String) {
        if (url.isEmpty().not()) {
            viewModel.getSubChapter.observe(viewLifecycleOwner) { data ->
                for (subChapter in data) {
                    if (subChapter.url == url) {
                        val subChapterFragmentDirections =
                            BodyFragmentDirections.actionBodyFragmentSelf(
                                BodyUrl(bodyFragmentArgs.bodyUrl.chapterEntity, subChapter), null
                            )
                        findNavController().navigate(subChapterFragmentDirections)
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.searchView) ChapterFragmentDirections.actionGlobalGlobalSearchFragment()
            .also { findNavController().navigate(it) }
        return super.onOptionsItemSelected(item)
    }


}