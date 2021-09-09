package org.apphatchery.gatbreferenceguide.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
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
import org.apphatchery.gatbreferenceguide.ui.adapters.FABodyNoteAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FABodyNoteColorAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeToDeleteCallback
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FABodyViewModel
import org.apphatchery.gatbreferenceguide.utils.*


@AndroidEntryPoint
class BodyFragment : BaseFragment(R.layout.fragment_body) {

    private lateinit var fragmentBodyBinding: FragmentBodyBinding
    private val bodyFragmentArgs: BodyFragmentArgs by navArgs()
    private lateinit var bodyUrl: BodyUrl
    private val viewModel: FABodyViewModel by viewModels()
    private var bookmarkEntity = BookmarkEntity()
    private lateinit var faBodyNoteColorAdapter: FABodyNoteColorAdapter
    private lateinit var faBodyNoteAdapter: FABodyNoteAdapter
    private var chartAndSubChapter: ChartAndSubChapter? = null
    private var bookmarkType: BookmarkType = BookmarkType.CHAPTER
    private lateinit var subChapterEntity: SubChapterEntity
    private lateinit var chapterEntity: ChapterEntity


    private fun isBookmark(subChapterId: Int) {
        viewModel.getBookmarkById(subChapterId).observe(viewLifecycleOwner) {
            if (it != null) {
                bookmarkEntity = it
                fragmentBodyBinding.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_star)
            } else {
                fragmentBodyBinding.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_star_outline)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentBodyBinding = FragmentBodyBinding.bind(view)
        bodyUrl = bodyFragmentArgs.bodyUrl
        setHasOptionsMenu(true)


        chartAndSubChapter = bodyFragmentArgs.chartAndSubChapter
        subChapterEntity = bodyUrl.subChapterEntity
        chapterEntity = bodyUrl.chapterEntity


        viewModel.recentOpen(
            RecentEntity(
                subChapterEntity.subChapterId,
                subChapterEntity.subChapterTitle
            )
        )

        isBookmark(subChapterEntity.subChapterId)

        faBodyNoteColorAdapter = FABodyNoteColorAdapter(requireContext()).also {
            it.submitList(NOTE_COLOR)
        }


        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = faBodyNoteAdapter.currentList[position]
                viewModel.deleteNote(note)
                fragmentBodyBinding.root.snackBar(" Note deleted.").also {
                    it.setAction("undo") {
                        viewModel.insertNote(note)
                    }
                }
            }
        }



        ItemTouchHelper(swipeHandler).also {
            it.attachToRecyclerView(fragmentBodyBinding.recyclerviewNote)
        }


        faBodyNoteAdapter = FABodyNoteAdapter().also {
            viewModel.getNote(subChapterEntity.subChapterId).observe(viewLifecycleOwner) { data ->
                it.submitList(data)
            }
        }


        setupWebView()
        fragmentBodyBinding.apply {

            toolbar.enableToolbar(requireActivity())
            toolbarBackButton.setOnClickListener { requireActivity().onBackPressed() }
            bookmarkImageButton.setOnClickListener { onBookmarkListener() }
//            addNote.setOnClickListener { onNoteListener() }

            if (chartAndSubChapter != null) isChartView() else {
                toolbarTitle.text = chapterEntity.chapterTitle
                textviewSubChapter.text = subChapterEntity.subChapterTitle
                bodyWebView.loadUrl(requireContext().cacheDir.toString() + '/' + PAGES_DIR + subChapterEntity.url + EXTENSION)
            }

            recyclerviewNote.apply {
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
                layoutManager = GridLayoutManager(requireContext(), 1)
                adapter = faBodyNoteAdapter
            }
        }

    }

    private fun isChartView() = fragmentBodyBinding.apply {
        viewModel.getChapterById(chartAndSubChapter!!.subChapterEntity.chapterId)
            .observeOnce(viewLifecycleOwner) { chapterEntity ->
                toolbarTitle.text = chapterEntity.chapterTitle


                tableViewLinearLayoutCompat.visibility = View.VISIBLE
                tableName.apply {
                    text = chartAndSubChapter!!.chartEntity.chartTitle
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
        bodyWebView.loadUrl(requireContext().cacheDir.toString() + '/' + PAGES_DIR + chartAndSubChapter!!.chartEntity.id + EXTENSION)

    }

    private fun onNoteListener() {
        Dialog(requireContext()).dialog().apply {
            setContentView(R.layout.dialog_note)
            val cancelButton = findViewById<View>(R.id.noteCancelButton)
            val saveButton = findViewById<View>(R.id.noteSaveButton)
            val noteBody = findViewById<TextInputEditText>(R.id.noteBody)
            val noteColorRecyclerView = findViewById<RecyclerView>(R.id.noteRecyclerViewColor)
            noteColorRecyclerView.apply {
                adapter = faBodyNoteColorAdapter
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
    }

    private fun onBookmarkListener() {
        if (bookmarkEntity.bookmarkId != 0) {
            requireContext().toast(bookmarkEntity.bookmarkTitle + " has been removed from your bookmarks.")
            viewModel.deleteBookmark(bookmarkEntity)
            bookmarkEntity = BookmarkEntity()
        } else
            Dialog(requireContext()).dialog().apply {
                setContentView(R.layout.dialog_bookmark)
                val cancelButton = findViewById<View>(R.id.bookmarkCancelButton)
                val saveButton = findViewById<View>(R.id.bookmarkSaveButton)
                val bookTitleTextInputEditText =
                    findViewById<TextInputEditText>(R.id.bookmarkTitleTextInputEditText)
                bookTitleTextInputEditText.also {
                    it.setText(subChapterEntity.subChapterTitle)
                    it.requestFocus()
                }

                cancelButton.setOnClickListener { dismiss() }

                saveButton.setOnClickListener {
                    onSaveBookmark(bookTitleTextInputEditText.text.toString().trim())
                    dismiss()
                }


                findViewById<TextView>(R.id.bookmarkTitle).text = subChapterEntity.subChapterTitle
                safeDialogShow()
            }

    }

    private fun onSaveBookmark(text: String) {
        if (bookmarkType != BookmarkType.CHART)
            bookmarkType = BookmarkType.SUBCHAPTER

        viewModel.insertBookmark(
            BookmarkEntity(
                bookmarkTitle = if (text.isEmpty()) subChapterEntity.subChapterTitle else text,
                bookmarkType = bookmarkType.toString(),
                subChapterId = subChapterEntity.subChapterId
            )
        )
        requireContext().toast("Bookmark saved")
    }

    private fun onSaveNote(noteBody: String) = fragmentBodyBinding.root.apply {
        if (noteBody.isBlank()) snackBar("Please enter notes to save.") else {
            viewModel.insertNote(
                NoteEntity(
                    subChapterId = subChapterEntity.subChapterId,
                    noteColor = faBodyNoteColorAdapter.selectedColor,
                    noteText = noteBody
                )
            )
            snackBar("Note saved")
        }
    }

    private fun setupWebView() = fragmentBodyBinding.bodyWebView.apply {
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
                val stripLink = link.substring(link.lastIndexOf("/") + 1, link.length)
                stripLink.replace(EXTENSION, "")
                gotoNavController(stripLink.replace(EXTENSION, ""))
                return super.shouldOverrideUrlLoading(view, request)
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
        when (item.itemId) {
            R.id.searchView -> {
                val directions = ChapterFragmentDirections.actionGlobalGlobalSearchFragment()
                findNavController().navigate(directions)
            }
        }
        return super.onOptionsItemSelected(item)
    }


}