package org.apphatchery.gatbreferenceguide.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentBodyBinding
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity
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


    /*if isSubChapterNull is true, chapter has no subchapter, use chapterId */
    private fun isSubChapterNull() = bodyFragmentArgs.bodyUrl.subChapterEntity == null

    private fun getBodyId() =
        if (isSubChapterNull()) bodyUrl.chapterEntity.chapterId else bodyUrl.subChapterEntity!!.subChapterId

    private fun getTitleOrUrl(): Array<String> {
        return arrayOf(
            (if (isSubChapterNull()) bodyUrl.chapterEntity.chapterTitle else bodyUrl.subChapterEntity!!.subChapterTitle),
            (if (isSubChapterNull()) {
                val text = bodyUrl.chapterEntity.chapterTitle
                text.lowercase()
                text.replace(" ", "_")
                bodyUrl.chapterEntity.chapterId.toString() + "_" + text
            } else bodyUrl.subChapterEntity!!.url),
        )
    }

    private fun isBookmark(id: Int, fetchByChapter: Boolean = false) {
        viewModel.getBookmarkById(id, fetchByChapter).observe(viewLifecycleOwner) {
            if (it != null) {
                bookmarkEntity = it
                fragmentBodyBinding.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_star)
            } else {
                fragmentBodyBinding.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_star_outline)
            }
        }
    }


    private fun ifSubChapterIsNull(bodyUrl: BodyUrl) {
        isBookmark(bodyUrl.chapterEntity.chapterId, true)
    }

    private fun ifSubChapterIsNotNull(bodyUrl: BodyUrl) {
        val subChapter = bodyUrl.subChapterEntity!!
        isBookmark(subChapter.subChapterId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentBodyBinding = FragmentBodyBinding.bind(view)
        bodyUrl = bodyFragmentArgs.bodyUrl
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

            viewModel.getNote(isSubChapterNull(), getBodyId()).observe(viewLifecycleOwner) { data ->
                it.submitList(data)
            }
        }

        if (isSubChapterNull()) ifSubChapterIsNull(bodyUrl) else {
            ifSubChapterIsNotNull(bodyUrl)
        }

        setupWebView()
        fragmentBodyBinding.apply {
            toolbar.setupToolbar(requireActivity(), bodyUrl.chapterEntity.chapterTitle)
            textviewSubChapter.text = getTitleOrUrl()[0]
            bodyWebView.loadUrl(requireContext().cacheDir.toString() + '/' + PAGES_DIR + getTitleOrUrl()[1] + EXTENSION)
            fragmentBodyBinding.bookmarkImageButton.setOnClickListener {
                onBookmarkListener()
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


            addNote.setOnClickListener { onNoteListener() }

        }


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
                    it.setText(getTitleOrUrl()[0])
                    it.requestFocus()
                }

                cancelButton.setOnClickListener { dismiss() }

                saveButton.setOnClickListener {
                    onSaveBookmark(bookTitleTextInputEditText.text.toString().trim())
                    dismiss()
                }


                findViewById<TextView>(R.id.bookmarkTitle).text = getTitleOrUrl()[0]
                safeDialogShow()
            }

    }

    private fun onSaveBookmark(text: String) {
        requireContext().toast("Bookmark saved")
        viewModel.insertBookmark(
            BookmarkEntity(
                bookmarkTitle = if (text.isEmpty()) getTitleOrUrl()[0] else text,
                isSubChapter = isSubChapterNull(),
                chapterId = bodyUrl.chapterEntity.chapterId,
                subChapterId = if (isSubChapterNull()) 0 else bodyUrl.subChapterEntity!!.subChapterId
            )
        )
    }

    private fun onSaveNote(noteBody: String) = fragmentBodyBinding.root.apply {
        if (noteBody.isBlank()) snackBar("Please enter notes to save.") else {
            viewModel.insertNote(
                NoteEntity(
                    subOrChapterId = getBodyId(),
                    isSubChapter = isSubChapterNull(),
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
            cacheMode = WebSettings.LOAD_NO_CACHE
        }


        webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {

                fragmentBodyBinding.bodyWebView.visibility = View.GONE
                fragmentBodyBinding.textviewSubChapter.visibility = View.GONE
                fragmentBodyBinding.view.visibility = View.GONE
                fragmentBodyBinding.linearLayoutCompat.visibility = View.GONE


                fragmentBodyBinding.page404.visibility = View.VISIBLE
                fragmentBodyBinding.backgroundImage.visibility = View.VISIBLE
                fragmentBodyBinding.coverView.visibility = View.VISIBLE


                super.onReceivedError(view, request, error)

            }



            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                 val link = request?.url.toString()
                Log.e(TAG, "shouldOverrideUrlLoading: "+ request!!.method )
                if (link.contains("#")) gotoTableId(link) else {
                    val stripLink = link.substring(link.lastIndexOf("/") + 1, link.length)
                    stripLink.replace(EXTENSION, "")
                    gotoNavController(stripLink.replace(EXTENSION, ""))
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }


    private fun gotoTableId(url: String) {
        Log.e(TAG, "gotoTableId: $url")
    }


    private fun gotoNavController(url: String) {
        Log.e(TAG, "gotoNavController: "+ url )
        if (url.isEmpty().not()) {
            viewModel.getSubChapter.observe(viewLifecycleOwner) { data ->
                for (subChapter in data) {
                    if (subChapter.url == url) {
                        val subChapterFragmentDirections =
                            BodyFragmentDirections.actionBodyFragmentSelf(
                                BodyUrl(bodyFragmentArgs.bodyUrl.chapterEntity, subChapter)
                            )
                        findNavController().navigate(subChapterFragmentDirections)
                    }
                }
            }
        }
    }


}