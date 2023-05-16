package org.apphatchery.gatbreferenceguide.ui.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentBodyBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.*
import org.apphatchery.gatbreferenceguide.enums.BookmarkType
import org.apphatchery.gatbreferenceguide.prefs.UserPrefs
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FANoteAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FANoteColorAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeDecoratorCallback
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FABodyViewModel
import org.apphatchery.gatbreferenceguide.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class BodyFragment : BaseFragment(R.layout.fragment_body) {


    companion object {
        const val DOMAIN_LINK = "https://apphatcherygatbreferenceguide.page.link"
        const val LOGO_URL =
            "https://raw.githubusercontent.com/AppHatchery/GA-TB-Reference-Guide-Web/main/assets/logo.jpg"
    }

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
    private lateinit var id: String
    private lateinit var title: String

    @Inject
    lateinit var userPrefs: UserPrefs

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

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
        bind.root.snackBar(getString(R.string.note_deleted)).also {
            it.setAction(getString(R.string.undo)) {
                viewModel.insertNote(note)
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentBodyBinding.bind(view)
        bodyUrl = bodyFragmentArgs.bodyUrl
        setHasOptionsMenu(true)

        baseURL = "file://" + requireContext().cacheDir.toString() + "/"

        chartAndSubChapter = bodyFragmentArgs.chartAndSubChapter
        subChapterEntity = bodyUrl.subChapterEntity
        chapterEntity = bodyUrl.chapterEntity


        bind.lastUpdateTextView.text =
            getString(R.string.last_updated, subChapterEntity.lastUpdated)


        getActionBar(requireActivity())?.title = HtmlCompat.fromHtml(chapterEntity.chapterTitle,FROM_HTML_MODE_LEGACY).toString()
        dialog = Dialog(requireContext()).dialog()



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

        if(bodyUrl.searchQuery.isNotEmpty() && !isOnlyWhitespace(bodyUrl.searchQuery)){
            bind.searchClearText.text = bodyUrl.searchQuery
            bind.searchClearContainer.visibility = View.VISIBLE
            bind.searchClearButton.setOnClickListener {

                bind.searchClearContainer.visibility = View.GONE
                bind.bodyWebView.apply {
                    clearMatches()//clears the search without multiple parameters
                    val lp = layoutParams as ViewGroup.MarginLayoutParams
                    lp.bottomMargin = 0
                    layoutParams = lp
                    //clears the search with multiple parameters
                    webViewClient = object : WebViewClient() {}
                    loadUrl(urlGlobal.toString())
                }
            }

            // add bottom margin
            bind.bodyWebView.apply {
                val lp = layoutParams as ViewGroup.MarginLayoutParams
                lp.bottomMargin = 20 + bind.searchClearContainer.height
                layoutParams = lp
            }
        }

        bind.apply {

            bookmarkImageButton.setOnClickListener { onBookmarkListener() }

            if (chartAndSubChapter != null) isChartView() else {

                val originalTitle = subChapterEntity.subChapterTitle
                val searchedWordToColor = bodyUrl.searchQuery
                val spannableString = SpannableString(originalTitle)
                val startIndex = originalTitle.indexOf(searchedWordToColor)
                if (startIndex != -1) {
                    val endIndex = startIndex + searchedWordToColor.length
                    val backgroundColorSpan = BackgroundColorSpan(Color.YELLOW)
                    spannableString.setSpan(backgroundColorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val foregroundColorSpan = ForegroundColorSpan(Color.BLACK)
                    spannableString.setSpan(foregroundColorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                textviewSubChapter.text = spannableString
                bodyWebView.loadUrl(baseURL + PAGES_DIR + subChapterEntity.url + EXTENSION)
            }


            id = if (bookmarkType == BookmarkType.CHART)
                chartAndSubChapter!!.chartEntity.id else
                subChapterEntity.subChapterId.toString()

            title = if (bookmarkType == BookmarkType.CHART)
                chartAndSubChapter!!.chartEntity.chartTitle else
                subChapterEntity.subChapterTitle


            addNote.setOnClickListener { onNoteListener() }

            faNoteAdapter = FANoteAdapter().also {
                viewModel.getNote(id).observe(viewLifecycleOwner) { data ->
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
                createDynamicLink()


            }

            shareFeedbackButton.setOnClickListener { onShareFeedbackListener() }
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


            if (chapterEntity.chapterId == 15) {
                bodyWebView.onZoomOut()
            }

        }



        setupBookmark(id)

        requireActivity().getBottomNavigationView()?.isChecked(R.id.mainFragment)

        /*Log screen name*/
        firebaseAnalytics.logEvent(
            ANALYTICS_PAGE_EVENT,
            bundleOf(Pair(ANALYTICS_PAGE_EVENT, subChapterEntity.url))
        )


        viewModel.recentOpen(RecentEntity(id, title))

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

        bodyWebView.onZoomOut()

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
        noteTitle.text = getString(R.string.edit_concat, getString(R.string.note))
        noteBody.apply {
            setText(note.noteText)
            setSelection(note.noteText.length)
            requestFocus()
        }

        deleteButton.apply {
            text = getString(R.string.delete)
            setOnClickListener {
                requireContext().alertDialog(
                    message = getString(R.string.note_confirm_deletion)
                ) {
                    dismiss()
                    viewModel.deleteNote(note)
                    onDeleteNoteSnackbar(note)
                }
            }
        }


        updateButton.apply {
            text = getString(R.string.update)
            setOnClickListener {
                if (noteBody.text.toString().trim()
                        .isEmpty()
                ) bind.root.snackBar(getString(R.string.note_enter_to_update_prompt)) else {
                    viewModel.updateNote(
                        note.copy(
                            noteText = noteBody.text.toString().trim(),
                            lastEdit = System.currentTimeMillis(),
                            noteColor = faNoteColorAdapter.selectedColor,
                        )
                    )
                    dismiss()
                    requireContext().toast(getString(R.string.note_updated))
                }
            }
        }
        safeDialogShow()
    }


    private fun onNoteListener() = Dialog(requireContext()).dialog().apply {
        setContentView(R.layout.dialog_note)
        val cancelButton = findViewById<AppCompatButton>(R.id.noteCancelButton)
        val saveButton = findViewById<AppCompatButton>(R.id.noteSaveButton)
        val noteBody = findViewById<AppCompatEditText>(R.id.noteBody)
        val noteColorRecyclerView = findViewById<RecyclerView>(R.id.noteRecyclerViewColor)
        findViewById<View>(R.id.closeDialog).setOnClickListener { dismiss() }
        noteColorRecyclerView.apply {
            adapter = faNoteColorAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        noteBody.requestFocus()
        cancelButton.apply {
            setCompoundDrawables(null, null, null, null)
            setOnClickListener { dismiss() }
        }

        saveButton.apply {
            setCompoundDrawables(null, null, null, null)
            setOnClickListener {
                onSaveNote(noteBody.text.toString().trim())
                dismiss()
            }
        }
        safeDialogShow()
    }

    private lateinit var dialog: Dialog

    @SuppressLint("SetJavaScriptEnabled")
    private fun onShareFeedbackListener() {
        requireView().snackBar("Working on it, just a moment please ...")
        dialog.apply {
            setContentView(R.layout.dialog_feedback)
            val page =
                if (bookmarkType == BookmarkType.CHART) chartAndSubChapter!!.chartEntity.id else subChapterEntity.url
            val url =
                "https://emorymedicine.sjc1.qualtrics.com/jfe/form/SV_4NEG4bjuyBGono9?page=$page"
            findViewById<WebView>(R.id.body_web_view).apply {
                settings.javaScriptEnabled = true
                loadUrl(url)
                webViewClient = object : WebViewClient() {

                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.let { if (it.progress == 100) safeDialogShow() }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        requireView().snackBar("Failed to process your request, please try again.")
                    }
                }
            }

            findViewById<View>(R.id.close_dialog).setOnClickListener { dismiss() }

        }

    }

    private fun onBookmarkListener() {

        Dialog(requireContext()).dialog().apply {
            setContentView(R.layout.dialog_bookmark)
            val cancelButton = findViewById<Button>(R.id.bookmarkCancelButton)
            val saveButton = findViewById<Button>(R.id.bookmarkSaveButton)
            val bookTitleTextInputEditText =
                findViewById<AppCompatEditText>(R.id.bookmarkTitleTextInputEditText)
            bookTitleTextInputEditText.also {
                it.setText(title)
                it.requestFocus()
            }

            cancelButton.setOnClickListener { dismiss() }

            saveButton.setOnClickListener {
                onSaveBookmark(bookTitleTextInputEditText.text.toString().trim())
                dismiss()
            }


            findViewById<View>(R.id.close_dialog).setOnClickListener { dismiss() }


            if (bookmarkEntity.bookmarkId != "0") {

                findViewById<TextView>(R.id.bookmarkTitle).text =
                    getString(R.string.edit_concat, getString(R.string.bookmark))
                bookTitleTextInputEditText.also {
                    it.setText(bookmarkEntity.bookmarkTitle)
                    it.setSelection(bookmarkEntity.bookmarkTitle.length)
                    it.requestFocus()
                }

                cancelButton.apply {
                    text = getString(R.string.delete)
                }


                saveButton.apply {
                    text = getString(R.string.update)
                }

                cancelButton.setOnClickListener {
                    requireContext().alertDialog(
                        message = getString(
                            R.string.bookmark_confirm_deletion,
                            bookmarkEntity.bookmarkTitle
                        )
                    ) {
                        dismiss()
                        viewModel.deleteBookmark(bookmarkEntity)
                        requireContext().toast(
                            getString(
                                R.string.bookmark_deleted,
                                bookmarkEntity.bookmarkTitle
                            )
                        )
                        bookmarkEntity = BookmarkEntity()
                    }
                }
                saveButton.setOnClickListener {
                    bookmarkEntity.copy(
                        bookmarkTitle = bookTitleTextInputEditText.text.toString().trim()
                    ).also {
                        dismiss()
                        viewModel.updateBookmark(it)
                        requireContext().toast(getString(R.string.bookmark_updated))
                    }
                }
            } else {
                cancelButton.setCompoundDrawables(null, null, null, null)
                saveButton.setCompoundDrawables(null, null, null, null)
            }

            safeDialogShow()
        }

    }

    private fun onSaveBookmark(text: String) {
        val bookmarkTitle = if (text.isEmpty()) subChapterEntity.subChapterTitle else text
        val bookmarkUrl =
            if (bookmarkType == BookmarkType.CHART) chartAndSubChapter!!.chartEntity.id else subChapterEntity.url

        viewModel.insertBookmark(
            BookmarkEntity(
                bookmarkTitle = bookmarkTitle,
                bookmarkId = id,
                subChapter = subChapterEntity.subChapterTitle
            )
        )


        /*Log bookmark name*/
        firebaseAnalytics.logEvent(
            ANALYTICS_BOOKMARK_EVENT,
            bundleOf(Pair(ANALYTICS_BOOKMARK_EVENT, bookmarkUrl))
        )

        firebaseAnalytics.logEvent(bookmarkUrl, null)

        requireContext().toast(getString(R.string.bookmark_saved))
    }

    private fun onSaveNote(noteBody: String) = bind.root.apply {
        if (noteBody.isBlank()) snackBar(getString(R.string.note_enter_to_save_prompt)) else {
            viewModel.insertNote(
                NoteEntity(
                    noteId = this@BodyFragment.id,
                    noteTitle = this@BodyFragment.title,
                    subChapterId = subChapterEntity.subChapterId,
                    noteColor = faNoteColorAdapter.selectedColor,
                    noteText = noteBody
                )
            )
            snackBar(getString(R.string.note_saved))
        }
    }
    var urlGlobal : String? = null

    private fun setupWebView() = bind.bodyWebView.apply {
        webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                 urlGlobal = url


                val searchInput = bodyUrl.searchQuery
                if(searchInput.isNotEmpty() && !isOnlyWhitespace(searchInput)){
                    Handler(Looper.getMainLooper()).postDelayed({
                        view?.findAllAsync(searchInput) }, 300) }else{ return }

                val allowedString = normalizeString(searchInput)
                val searchBody = allowedString.split(" ")

                for (eachWord in searchBody) {
                    val jsCode = "javascript:(function() { " +
                            "var count = 0;" +
                            "function highlightAllOccurencesOfString(str) {" +
                            "  var obj = window.document.getElementsByTagName('body')[0];" +
                            "  var html = obj.innerHTML;" +
                            "  var regex = new RegExp('(?<!<[^>]*>)' + str + '(?![^<]*?>)', 'gi');" +
                            "  var allOccurrences = html.match(regex);" +
                            "  count = allOccurrences.length;" +
                            "  for (var i = 0; i < count; i++) {" +
                            "    var occurrence = allOccurrences[i];" +
                            "    var span = document.createElement('span');" +
                            "    span.style.backgroundColor = 'yellow';" +
                            "    span.style.color = 'black';" +
                            "    span.style.fontWeight = 'normal';" +
                            "    span.innerHTML = occurrence;" +
                            "    html = html.replace(new RegExp('(?<!<[^>]*>)' + occurrence + '(?![^<]*?>)', 'gi'), span.outerHTML);" +
                            "  }" +
                            "  obj.innerHTML = html;" +
                            "}" +
                            "highlightAllOccurencesOfString('$eachWord');" +
                            "})()"
                    view?.loadUrl(jsCode)
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val link = request?.url.toString()
                return when {
                    link.subSequence(0, 4).toString().lowercase() == "http".lowercase() -> {
                        requireActivity().apply {
                            alertDialog("", message = getString(R.string.open_link_in_browser)) {
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
    fun isOnlyWhitespace(str: String): Boolean {
        val trimmedStr = str.trim()
        return trimmedStr.isEmpty()
    }
    fun normalizeString(str: String): String {

        // Remove any leading or trailing spaces
        var normalizedStr = str.trim()

        // Replace multiple spaces with a single space
        normalizedStr = normalizedStr.replace("\\s+".toRegex(), " ")

        // Remove any spaces that are not in between two words
        normalizedStr = normalizedStr.replace("\\s([\\W\\s]*)\\s".toRegex(), "$1")

        return normalizedStr
    }


    private fun gotoNavController(url: String) {
        if (url.isEmpty().not()) {
            viewModel.getSubChapter.observe(viewLifecycleOwner) { data ->
                for (subChapter in data) {
                    if (subChapter.url == url) {
                        val subChapterFragmentDirections =
                            BodyFragmentDirections.actionBodyFragmentSelf(
                                BodyUrl(bodyFragmentArgs.bodyUrl.chapterEntity, subChapter, ""), null
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

    private fun isBookmarkCheck() = bookmarkType == BookmarkType.CHART

    private fun createDynamicLink() {
        requireContext().toast(getString(R.string.dynamic_link_generation))
         val androidQueryId = id
        val androidIsPage = if (isBookmarkCheck()) 0 else 1
        val iosHtmlFile = if (isBookmarkCheck()) chartAndSubChapter!!.chartEntity.id else
            subChapterEntity.url

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("$DOMAIN_LINK?androidQueryId=$androidQueryId&androidIsPage=$androidIsPage&chapterID=$iosHtmlFile"))
            .setDomainUriPrefix(DOMAIN_LINK)
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .setIosParameters(DynamicLink.IosParameters
                .Builder("edu.emory.tb.guide")
                .setAppStoreId("1583294462")
                .build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle(chapterEntity.chapterTitle)
                    .setDescription(subChapterEntity.subChapterTitle)
                    .setImageUrl(Uri.parse(LOGO_URL))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { result ->
                Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, result.shortLink.toString())
                    .setType("text/plain")
                    .also {
                        requireActivity().startActivity(
                            Intent.createChooser(
                                it,
                                getString(R.string.share)
                            )
                        )
                    }
            }
            .addOnFailureListener {
                Log.e(TAG, "createDynamicLink: ", it)
                requireContext().toast(getString(R.string.dynamic_link_failed_to_generate))
            }
    }

}