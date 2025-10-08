package org.apphatchery.gatbreferenceguide.ui.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.view.MenuHost
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentBodyBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.BookmarkEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.NoteEntity
import org.apphatchery.gatbreferenceguide.db.entities.RecentEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.enums.BookmarkType
import org.apphatchery.gatbreferenceguide.prefs.UserPrefs
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FANoteAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FANoteColorAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.SwipeDecoratorCallback
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FABodyViewModel
import org.apphatchery.gatbreferenceguide.utils.ANALYTICS_BOOKMARK_EVENT
import org.apphatchery.gatbreferenceguide.utils.ANALYTICS_PAGE_EVENT
import org.apphatchery.gatbreferenceguide.utils.EXTENSION
import org.apphatchery.gatbreferenceguide.utils.NOTE_COLOR
import org.apphatchery.gatbreferenceguide.utils.PAGES_DIR
import org.apphatchery.gatbreferenceguide.utils.alertDialog
import org.apphatchery.gatbreferenceguide.utils.dialog
import org.apphatchery.gatbreferenceguide.utils.getBottomNavigationView
import org.apphatchery.gatbreferenceguide.utils.isChecked
import org.apphatchery.gatbreferenceguide.utils.observeOnce
import org.apphatchery.gatbreferenceguide.utils.safeDialogShow
import org.apphatchery.gatbreferenceguide.utils.searchState
import org.apphatchery.gatbreferenceguide.utils.snackBar
import org.apphatchery.gatbreferenceguide.utils.toast
import sdk.pendo.io.Pendo
import javax.inject.Inject
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import android.text.TextWatcher
import android.text.Editable
import android.text.SpannableStringBuilder
import androidx.activity.OnBackPressedCallback
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.google.android.material.bottomsheet.BottomSheetDialog

@AndroidEntryPoint
class BodyFragment : BaseFragment(R.layout.fragment_body) {

    private val TAG = "MyFragmentLifecycle"

    companion object {
        const val DOMAIN_LINK = "https://gatbreferenceguide.page.link"
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
    private lateinit var searchContainer: RelativeLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchClear: ImageView
    private lateinit var searchCounter: TextView
    private lateinit var searchPrevious: ImageView
    private lateinit var searchNext: ImageView
    private var backPressedCallback: OnBackPressedCallback? = null
    private var isExpanded = false
    private var currentMatch = 0
    private var totalMatches = 0
    private lateinit var chapterEntity: ChapterEntity
    private var baseURL = ""
    private var filesURL = ""
    private var isCollapsed = false
    private lateinit var id: String
    private lateinit var title: String

    private lateinit var webViewFont: WebView
    private lateinit var sharedPreferences: SharedPreferences
    private var fontValue: Array<String> = arrayOf("Small", "Normal", "Large", "Larger")

    @Inject
    lateinit var userPrefs: UserPrefs

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics


    private fun setupBookmark(id: String) {
        viewModel.getBookmarkById(id).observe(viewLifecycleOwner) {
            if (it != null) {
                bookmarkEntity = it
                bind.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_folder_bookmarked)
                bind.isBookmarkedText.setText(R.string.bookmarked)
            } else {
                bind.bookmarkImageButton.setImageResource(R.drawable.ic_baseline_folder_outline)
                bind.isBookmarkedText.setText(R.string.bookmark)
            }
        }
    }

    private fun onDeleteNoteSnackbar(note: NoteEntity) {
        showNoteDeletedCard("Note Deleted")
        // undo functionality with a delay
        // Handler(Looper.getMainLooper()).postDelayed({
        // }, 3000)
    }

    private fun updateFont() {
        val fontIndex =
            sharedPreferences.getString(getString(R.string.font_key), "1")?.toInt() ?: 1
        val fontSize = when (fontIndex) {
            0 -> 100 // Small
            1 -> 125// Normal
            2 -> 150 // Large
            3 -> 175 // Larger
            else -> 125
        }

        webViewFont.settings.textZoom = fontSize
    }

    private fun showFontDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_font_settings, null)
        val fontSettingsSlider: Slider = dialogView.findViewById(R.id.font_size_slider)
        val fontSizeText: TextView = dialogView.findViewById(R.id.font_size_text)
        val closeDialogButton: ImageButton = dialogView.findViewById(R.id.close_dialog_button)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.show()

        closeDialogButton.setOnClickListener { dialog.dismiss() }
        val currentFontSizeIndex =
            sharedPreferences.getString(getString(R.string.font_key), "1")?.toInt() ?: 1

        when (currentFontSizeIndex) {
            0 -> {
                fontSizeText.setText(R.string.font_size_small)
                fontSettingsSlider.value = 100F
            }

            1 -> {
                fontSizeText.setText(R.string.font_size_normal)
                fontSettingsSlider.value = 125F
            }

            2 -> {
                fontSizeText.setText(R.string.font_size_large)
                fontSettingsSlider.value = 150F
            }

            else -> {
                fontSizeText.setText(R.string.font_size_larger)
                fontSettingsSlider.value = 175F
            }
        }

        fontSettingsSlider.addOnChangeListener { _, value, _ ->
            val selectedIndex = when (value) {
                100F -> 0
                125F -> 1
                150F -> 2
                175F -> 3
                else -> 1
            }

            when (selectedIndex) {
                0 ->
                    fontSizeText.setText(R.string.font_size_small)

                1 ->
                    fontSizeText.setText(R.string.font_size_normal)

                2 ->
                    fontSizeText.setText(R.string.font_size_large)

                3 ->
                    fontSizeText.setText(R.string.font_size_larger)

                else -> fontSizeText.setText(R.string.font_size_normal)
            }

            sharedPreferences.edit()
                .putString(getString(R.string.font_key), selectedIndex.toString())
                .apply()
            updateFont()
        }
    }

    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == getString(R.string.font_key)) {
                updateFont()
            }
        }

    override fun onDestroyView() {
        backPressedCallback?.isEnabled = false
        super.onDestroyView()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onPause() {
        // Ensure keyboard is dismissed when leaving this screen 
        hideKeyboard()
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentBodyBinding.bind(view)
        bodyUrl = bodyFragmentArgs.bodyUrl
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

        webViewFont = bind.bodyWebView

        webViewFont.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true // 10
            allowContentAccess = true
            // Enable smooth scrolling for better search result navigation
            setSupportZoom(true)
            builtInZoomControls = false
        }

        val menuHost: MenuHost = requireActivity()

       // baseURL = "file://" + requireContext().cacheDir.toString() + "/"
        baseURL = "file://" + requireContext().cacheDir.toString() + "/"
        filesURL = "file://" + requireContext().filesDir.absolutePath.toString() + "/"

        chartAndSubChapter = bodyFragmentArgs.chartAndSubChapter
        subChapterEntity = bodyUrl.subChapterEntity
        chapterEntity = bodyUrl.chapterEntity

        val contentTitle = if (chartAndSubChapter != null) {
            // For charts, use the chart title from the list
            chartAndSubChapter!!.chartEntity.chartTitle
        } else {
            // For regular content, use the subchapter title
            HtmlCompat.fromHtml(subChapterEntity.subChapterTitle, FROM_HTML_MODE_LEGACY).toString()
        }
    setActionBarTitle(contentTitle)

        // Setup search functionality only for subchapter content (not charts)
        if (chartAndSubChapter == null) {
            setupSearch()
        } else {
            // Hide search view for charts
            bind.searchViewInclude.root.visibility = View.GONE
        }

//            menuHost.addMenuProvider(object : MenuProvider {
//                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                    menuInflater.inflate(R.menu.search_menu, menu)
//                }
//
//                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                    return handleMenuItemSelection(menuItem)
//                }
//            }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        faNoteColorAdapter = FANoteColorAdapter(requireContext()).also {
            it.submitList(NOTE_COLOR)
        }




        setupWebView()
//search query
        if (bodyUrl.searchQuery.isNotEmpty() && !isOnlyWhitespace(bodyUrl.searchQuery)) {
            // Do not display the bottom search-clear container anymore
            bind.searchClearContainer.visibility = View.GONE
        }

        bind.apply {

            bookmarkImageButton.setOnClickListener { onBookmarkListener() }

            if (chartAndSubChapter != null) isChartView() else {
                // Regular content: position content container and load the WebView
                val contentContainer = root.findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.content_container)
                val layoutParams = contentContainer?.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                layoutParams?.let {
                    it.topToBottom = R.id.search_view_include
                    it.topMargin = 0
                    contentContainer.layoutParams = it
                }

                val loadUrl = baseURL + PAGES_DIR + subChapterEntity.url + EXTENSION
                val fileFromDir = filesURL + subChapterEntity.url + EXTENSION
                if (subChapterEntity.url == "15_appendix_district_tb_coordinators_(by_district)") {
                    bodyWebView.loadUrl(fileFromDir)
                } else {
                    bodyWebView.loadUrl(loadUrl)
                }
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
                }

                it.itemClickCallback { onNoteListenerEdit(it) }
            }

            shareButton.setOnClickListener {
                createDynamicLink()
            }

            homeButton.setOnClickListener {
                hideKeyboard() // Dismiss keyboard before navigating to home
                findNavController().popBackStack(R.id.mainFragment, false)
            }

            changeFontSizeButton.setOnClickListener {
                showFontDialog()
            }





        }

        setupBookmark(id)

        // Setup floating button 
        if (chartAndSubChapter == null) {
            // floating button for subchapters
            setupFloatingButton(false)
        } else {
            // Position floating button 24dp from top for charts
            setupFloatingButton(true)
        }

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
                //                override fun onAnimationStart(animation: Animator?) = Unit
//                override fun onAnimationEnd(animation: Animator?) = onAnimationCompleted()
//                override fun onAnimationCancel(animation: Animator?) = Unit
//                override fun onAnimationRepeat(animation: Animator?) = Unit
                override fun onAnimationStart(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                }

                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationRepeat(p0: Animator) {
                }


            })
        }


    private fun isChartView() = bind.apply {
        // For chart content, position content container and load the chart URL
        val contentContainer = root.findViewById<androidx.appcompat.widget.LinearLayoutCompat>(R.id.content_container)
        val layoutParams = contentContainer?.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        layoutParams?.let {
            it.topToBottom = R.id.search_view_include
            it.topMargin = 0
            contentContainer.layoutParams = it
        }

        bookmarkType = BookmarkType.CHART
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
        val editTagLabel = findViewById<TextView>(R.id.editTagLabel)
        val feedbackContainer = findViewById<RelativeLayout>(R.id.feedback_container)
        
        noteColorRecyclerView.apply {
            faNoteColorAdapter.selectedColor = note.noteColor
            adapter = faNoteColorAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        findViewById<View>(R.id.closeDialog).setOnClickListener { dismiss() }
        noteTitle.text = getString(R.string.edit_concat, getString(R.string.note))
        editTagLabel.text = "Edit Tag" // Change from "Add tag" to "Edit tag"
        feedbackContainer.visibility = View.GONE // Hide feedback container in edit mode
        
        noteBody.apply {
            setText(note.noteText)
            setSelection(note.noteText.length)
            requestFocus()
        }

        deleteButton.apply {
            text = getString(R.string.delete)
            setOnClickListener {
                showNoteDeletionConfirmationPopup(
                    onEditDialogHide = { hide() }, 
                    onEditDialogShow = { show() }, 
                    onEditDialogDismiss = { dismiss() },
                    onConfirm = {
                        viewModel.deleteNote(note)
                        onDeleteNoteSnackbar(note)
                    }
                )
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
                    showNoteDeletedCard("Note Updated")
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
        val checkBox = findViewById<CheckBox>(R.id.select)
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
                val isSubmitFeedback = checkBox?.isChecked ?: false
                onSaveNote(noteBody.text.toString().trim(), isSubmitFeedback)
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
            val cancelButton = findViewById<TextView>(R.id.bookmarkCancelButton)
            val saveButton = findViewById<TextView>(R.id.bookmarkSaveButton)
            val bookTitleTextInputEditText =
                findViewById<AppCompatEditText>(R.id.bookmarkTitleTextInputEditText)
            bookTitleTextInputEditText.also {
                it.setText(title)
                it.requestFocus()
            }

            cancelButton.setOnClickListener { dismiss() }

            saveButton.setOnClickListener {
                val enteredTitle = bookTitleTextInputEditText.text.toString().trim()
                if (enteredTitle.isEmpty()) {
                    bookTitleTextInputEditText.error = getString(R.string.bookmark_title_required)
                    return@setOnClickListener
                }
                onSaveBookmark(enteredTitle)
                dismiss()
            }

            // clears the text field
            findViewById<View>(R.id.close_dialog).setOnClickListener {
                bookTitleTextInputEditText.setText("")
            }


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
                    showBookmarkDeletionConfirmation(
                        bookmarkEntity,
                        onEditDialogHide = { hide() }, 
                        onEditDialogShow = { show() }, 
                        onEditDialogDismiss = { dismiss() } 
                    )
                }
                saveButton.setOnClickListener {
                    val newTitle = bookTitleTextInputEditText.text.toString().trim()
                    if (newTitle.isEmpty()) {
                        bookTitleTextInputEditText.error = getString(R.string.bookmark_title_required)
                        return@setOnClickListener
                    }
                    bookmarkEntity.copy(
                        bookmarkTitle = newTitle
                    ).also {
                        dismiss()
                        viewModel.updateBookmark(it)
                        showNoteDeletedCard(getString(R.string.bookmark_updated))
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

        showBookmarkSuccessPopup()
    }

    private fun showBookmarkSuccessPopup() {
        Dialog(requireContext()).dialog().apply {
            setContentView(R.layout.dialog_bookmark_success)
            
            val bookmarkedText = findViewById<TextView>(R.id.bookmarked_text)
            val visitButton = findViewById<AppCompatButton>(R.id.visit_button)
            val dismissButton = findViewById<AppCompatButton>(R.id.dismiss_button)
            
            // Set the text with HTML formatting to make "My Bookmarks" and "Home" highlighted
            bookmarkedText.text = HtmlCompat.fromHtml(
                getString(R.string.bookmarked_message), 
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            
            visitButton.setOnClickListener {
                dismiss()
                hideKeyboard() // Dismiss keyboard before navigating to home
                // Navigate to bookmarks/home page
                findNavController().popBackStack(R.id.mainFragment, false)
            }
            
            dismissButton.setOnClickListener {
                dismiss()
            }
            
            safeDialogShow()
        }
    }

    private fun showNoteDeletionConfirmationPopup(onEditDialogHide: () -> Unit, onEditDialogShow: () -> Unit, onEditDialogDismiss: () -> Unit, onConfirm: () -> Unit) {
        // Hide edit dialog 
        onEditDialogHide()
        
        Dialog(requireContext()).dialog().apply {
            setContentView(R.layout.dialog_note_deletion_confirmation)

            val yesButton = findViewById<AppCompatButton>(R.id.cancelButton)
            val cancelButton = findViewById<AppCompatButton>(R.id.yesButton)

            yesButton.setOnClickListener {
                dismiss()
                onConfirm()
                onEditDialogDismiss()
            }

            cancelButton.setOnClickListener {
                dismiss()
                onEditDialogShow()
            }

            safeDialogShow()
        }
    }

    private fun onSaveNote(noteBody: String, isSubmitFeedback: Boolean = false) = bind.root.apply {
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
            // Show confirmation card
            showNoteDeletedCard("Note Saved")
            
            // Show thank you dialog and send feedback to Pendo if checkbox was ticked
            if (isSubmitFeedback) {
                // Send note body to Pendo as feedback
                sendNoteToPendo(noteBody)
                
                Handler(Looper.getMainLooper()).postDelayed({
                    showThankYouDialog()
                }, 3000) 
            }
        }
    }

    var urlGlobal: String? = null

    private fun setupWebView() = bind.bodyWebView.apply {
        onZoomOut()
        
        // JavaScript interface for search results
        addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun onSearchResultsFound(count: Int) {
                requireActivity().runOnUiThread {
                    totalMatches = count
                    currentMatch = if (totalMatches > 0) 1 else 0
                    updateSearchUI()
                    Log.d("BodyFragment", "Found $totalMatches search results")
                }
            }
        }, "AndroidInterface")
        
        webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                urlGlobal = url


                val searchInput = bodyUrl.searchQuery
                if (searchInput.isNotEmpty() && !isOnlyWhitespace(searchInput)) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        view?.findAllAsync(searchInput)
                    }, 300)
                } else {
                    return
                }

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


            override fun onReceivedError(view: WebView?, request: WebResourceRequest?,   error:  WebResourceError?) {
                super.onReceivedError(view, request, error)
                // fire pendo track event
                if (error?.description.toString().contains("net::ERR_FILE_NOT_FOUND")) {
                    val properties = hashMapOf<String, Any>()
                    properties["error"] = error.toString()
                    properties["error_desc"] = error?.description.toString()
                    Pendo.track("missing_content", properties)
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

                    link.contains("mailto:") -> {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse(link)
                        }
                        try{
                            startActivity(emailIntent)
                        } catch(e: ActivityNotFoundException){
                            Toast.makeText(requireContext(), "No email client found", Toast.LENGTH_SHORT).show()
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
                                BodyUrl(bodyFragmentArgs.bodyUrl.chapterEntity, subChapter, ""),
                                null
                            )
                        findNavController().navigate(subChapterFragmentDirections)
                    }
                }
            }
        }
    }




    private fun handleMenuItemSelection(item: MenuItem): Boolean {
                if (searchState.currentState.toString() == "IN_SEARCH") {
            if (item.itemId == R.id.searchView) {
                val comp = findNavController().popBackStack(R.id.globalSearchFragment, false)
                if (!comp) {
                    if (item.itemId == R.id.searchView) SubChapterFragmentDirections.actionGlobalGlobalSearchFragment()
                        .also {
                            findNavController().navigate(it)
                        }
                }
            }

        } else {
            if (item.itemId == R.id.searchView) BodyFragmentDirections.actionGlobalGlobalSearchFragment()
                .also {
                    findNavController().navigate(it)
                }
        }

        return false
    }



    private fun isBookmarkCheck() = bookmarkType == BookmarkType.CHART

    private fun createDynamicLink() {
        createDynamicLink { link ->
            Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, link)
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
    }

    private fun createDynamicLink(onLinkGenerated: (String) -> Unit) {
        requireContext().toast(getString(R.string.dynamic_link_generation))
        val androidQueryId = id
        val androidIsPage = if (isBookmarkCheck()) 0 else 1
        val iosHtmlFile = if (isBookmarkCheck()) chartAndSubChapter!!.chartEntity.id else
            subChapterEntity.url

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("$DOMAIN_LINK?androidQueryId=$androidQueryId&androidIsPage=$androidIsPage&chapterID=$iosHtmlFile"))
            .setDomainUriPrefix(DOMAIN_LINK)
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .setIosParameters(
                DynamicLink.IosParameters
                    .Builder("edu.emory.tb.guide")
                    .setAppStoreId("1583294462")
                    .build()
            )
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle(chapterEntity.chapterTitle)
                    .setDescription(subChapterEntity.subChapterTitle)
                    .setImageUrl(Uri.parse(LOGO_URL))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { result ->
                onLinkGenerated(result.shortLink.toString())
            }
            .addOnFailureListener {
                Log.e(TAG, "createDynamicLink: ", it)
                requireContext().toast(getString(R.string.dynamic_link_failed_to_generate))
            }
    }

    private fun setupSearch() {
        Log.d("BodyFragment", "Setting up search...")
        
        // Initialize the search container and views
        searchContainer = bind.searchViewInclude.root as RelativeLayout
        searchEditText = searchContainer.findViewById(R.id.search_edit_text)
        searchClear = searchContainer.findViewById(R.id.search_clear)
        searchCounter = searchContainer.findViewById(R.id.search_counter)
        searchPrevious = searchContainer.findViewById(R.id.search_previous)
        searchNext = searchContainer.findViewById(R.id.search_next)
        
        // Get reference to the search controls container 
        val searchControlsContainer = searchContainer.findViewById<LinearLayout>(R.id.search_controls_container)
        
        // Initially hide the search view
        searchContainer.visibility = View.GONE
        
        // Show the search view when content is loaded
        showSearchView()
        
        // Setup search listeners
        setupSearchListeners()
        
        // Setup back press handling to collapse search when expanded
        backPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (searchContainer.visibility == View.VISIBLE && isExpanded) {
                    // Dismiss keyboard and collapse the search UI on back press while searching
                    hideKeyboard()
                    collapseSearchView()
                    isEnabled = false
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback!!)
        
        Log.d("BodyFragment", "Search setup completed")
    }

    private fun showSearchView() {
        searchContainer.visibility = View.VISIBLE
        backPressedCallback?.isEnabled = true
    }

    private fun setupSearchListeners() {
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !isExpanded) {
                expandSearchView()
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                
                // Show/hide search counter based on text
                searchCounter.visibility = if (query.isBlank()) View.GONE else View.VISIBLE
                
                if (query.isNotEmpty()) {
                    searchClear.visibility = View.VISIBLE
                    if (isExpanded) {
                        performInPageSearch(query)
                    }
                } else {
                    // Clear WebView search highlights when search text is cleared
                    clearWebViewSearch()
                    collapseSearchView()
                    searchClear.visibility = View.GONE
                    if (isExpanded) {
                        showEmptySearchState()
                    }
                }
            }
        })

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    if (!isExpanded) {
                        expandSearchView()
                    }
                    performInPageSearch(query)
                }
                true
            } else false
        }

        searchClear.setOnClickListener {
            // Clear the search text
            searchEditText.text.clear()
            // Clear WebView search
            clearWebViewSearch()
            // Collapse the search view
            collapseSearchView()
            // Dismiss keyboard when clearing search
            hideKeyboard()
        }

        searchPrevious.setOnClickListener {
            hideKeyboard()
            navigatePrevious()
        }
        searchNext.setOnClickListener {
            hideKeyboard()
            navigateNext()
        }
    }

    private fun expandSearchView() {
        if (isExpanded) return
        isExpanded = true

        // Calculate available space for EditText based on screen width and controls container
        val screenWidth = resources.displayMetrics.widthPixels
        val containerPadding = searchContainer.paddingStart + searchContainer.paddingEnd
        val controlsWidth = (120 * resources.displayMetrics.density).toInt() // Approximate width for controls
        val targetWidth = maxOf((screenWidth - containerPadding - controlsWidth), (160 * resources.displayMetrics.density).toInt())

        ValueAnimator.ofInt(searchEditText.width, targetWidth).apply {
            duration = 250
            addUpdateListener { animation ->
                val layoutParams = searchEditText.layoutParams
                layoutParams.width = animation.animatedValue as Int
                searchEditText.layoutParams = layoutParams
            }
            doOnEnd {
                // Change background and show controls
                searchEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.search_input_background)
                showSearchControls()
                
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchClear.visibility = View.VISIBLE
                    performInPageSearch(query)
                } else {
                    showEmptySearchState()
                }
            }
        }.start()
    }

    private fun showSearchControls() {
        searchClear.visibility = View.VISIBLE
        val searchControlsContainer = searchContainer.findViewById<LinearLayout>(R.id.search_controls_container)
        searchControlsContainer.visibility = View.VISIBLE
    }

    private fun showEmptySearchState() {
        totalMatches = 0
        currentMatch = 0
        searchCounter.text = "0/0"
       
        val searchControlsContainer = searchContainer.findViewById<LinearLayout>(R.id.search_controls_container)
        searchControlsContainer.visibility = View.VISIBLE
        searchClear.visibility = View.VISIBLE
        Log.d("BodyFragment", "Showing empty search state")
    }

    private fun collapseSearchView() {
        if (!isExpanded) return
        isExpanded = false

        // Hide controls
        val searchControlsContainer = searchContainer.findViewById<LinearLayout>(R.id.search_controls_container)
        searchControlsContainer.visibility = View.GONE
        searchClear.visibility = View.GONE

        // Animate back to full width
        val targetWidth = ViewGroup.LayoutParams.MATCH_PARENT
        val parentWidth = (searchContainer.parent as? View)?.width ?: resources.displayMetrics.widthPixels
        val actualTargetWidth = parentWidth - searchContainer.paddingStart - searchContainer.paddingEnd

        ValueAnimator.ofInt(searchEditText.width, actualTargetWidth).apply {
            duration = 250
            addUpdateListener { animation ->
                val layoutParams = searchEditText.layoutParams
                layoutParams.width = animation.animatedValue as Int
                searchEditText.layoutParams = layoutParams
            }
            doOnEnd {
                val layoutParams = searchEditText.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                searchEditText.layoutParams = layoutParams
                searchEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.search_input_background)
            }
        }.start()

        searchEditText.clearFocus()
        clearWebViewSearch()
    }

    private fun performInPageSearch(searchQuery: String) {
        if (searchQuery.isNotEmpty()) {
            // Set up the search result listener
            bind.bodyWebView.setOnSearchResultListener { totalMatches, currentMatch ->
                this.totalMatches = totalMatches
                this.currentMatch = currentMatch
                updateSearchUI()
            }
            
            // Perform WebView search
            bind.bodyWebView.findAllAsync(searchQuery)
            bind.searchClearContainer.visibility = View.GONE
            
            Log.d("BodyFragment", "Performing search for: $searchQuery")
        }
    }

    private fun updateSearchCount(query: String) {
        // Using JavaScript to count actual occurrences in the WebView content
        val jsCode = """
            javascript:(function() {
                var searchText = '$query';
                var bodyText = document.body.innerText || document.body.textContent || '';
                var regex = new RegExp(searchText.replace(/[.*+?^${'$'}{}()|[\]\\]/g, '\\$&'), 'gi');
                var matches = bodyText.match(regex);
                var count = matches ? matches.length : 0;
                window.AndroidInterface.onSearchResultsFound(count);
            })();
        """.trimIndent()
        
        bind.bodyWebView.evaluateJavascript(jsCode) { result ->
            // If JavaScript fails, use fallback estimation
            if (result == null || result == "null") {
                totalMatches = when {
                    query.length < 2 -> 0
                    query.length < 3 -> 1
                    query.length < 5 -> 2
                    query.length < 7 -> 4
                    else -> 6
                }
                currentMatch = if (totalMatches > 0) 1 else 0
                updateSearchUI()
                Log.d("BodyFragment", "Fallback: Estimated $totalMatches matches for query: $query")
            }
        }
    }

    private fun updateSearchUI() {
        val count = totalMatches
        searchCounter.text = "${currentMatch}/${count}"
        
        val searchControlsContainer = searchContainer.findViewById<LinearLayout>(R.id.search_controls_container)
        searchControlsContainer.visibility = View.VISIBLE
        searchClear.visibility = View.VISIBLE
        
        Log.d("BodyFragment", "Updated search UI: ${currentMatch}/${count}")
    }

    private fun navigatePrevious() {
        if (totalMatches > 0) {
            // Navigate to previous match in WebView and update counter
            bind.bodyWebView.findNext(false)
            
            // Update current match counter (cycle from 1 to totalMatches)
            currentMatch = if (currentMatch <= 1) totalMatches else currentMatch - 1
            updateSearchCounter()
            
            // Ensure smooth scrolling to the highlighted result
            ensureSearchResultVisible()
            
            Log.d("BodyFragment", "Navigated to previous match: ${currentMatch}/${totalMatches}")
        }
    }

    private fun navigateNext() {
        if (totalMatches > 0) {
            // Navigate to next match in WebView and update counter
            bind.bodyWebView.findNext(true)
            
            // Update current match counter (cycle from 1 to totalMatches)
            currentMatch = if (currentMatch >= totalMatches) 1 else currentMatch + 1
            updateSearchCounter()
            
            // Ensure smooth scrolling to the highlighted result
            ensureSearchResultVisible()
            
            Log.d("BodyFragment", "Navigated to next match: ${currentMatch}/${totalMatches}")
        }
    }
    
    private fun ensureSearchResultVisible() {
        // JavaScript to ensure the current highlighted search result is properly centered in view
        val jsCode = """
            javascript:(function() {
                // Find the currently highlighted search result
                var highlighted = document.querySelector('span[style*="background-color: yellow"], span[style*="background: yellow"]');
                if (highlighted) {
                    // Scroll to the highlighted element with smooth behavior
                    highlighted.scrollIntoView({ 
                        behavior: 'smooth', 
                        block: 'center',
                        inline: 'center'
                    });
                }
            })();
        """.trimIndent()
        
        // Execute with a small delay to ensure WebView has processed findNext
        Handler(Looper.getMainLooper()).postDelayed({
            bind.bodyWebView.evaluateJavascript(jsCode, null)
        }, 100)
    }

    private fun updateSearchCounter() {
        val count = totalMatches
        searchCounter.text = "${currentMatch}/${count}"
        Log.d("BodyFragment", "Updated search counter: ${currentMatch}/${count}")
    }

    private fun clearWebViewSearch() {
        bind.bodyWebView.clearMatches()
        bind.searchClearContainer.visibility = View.GONE
        Log.d("BodyFragment", "Cleared WebView search")
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun showNoteDeletedCard(message: String) {
        val container = view?.findViewById<RelativeLayout>(R.id.delete_card_container)
        val textView = view?.findViewById<TextView>(R.id.delete_card_text)
        
        if (container != null && textView != null) {
            textView.text = message
            container.visibility = View.VISIBLE
            
            // Auto-hide after 3 seconds
            container.postDelayed({
                container.visibility = View.GONE
            }, 3000)
        }
    }

    private fun showBookmarkDeletionConfirmation(bookmark: BookmarkEntity, onEditDialogHide: () -> Unit, onEditDialogShow: () -> Unit, onEditDialogDismiss: () -> Unit) {
        // Hide edit dialog 
        onEditDialogHide()
        
        // Show bookmark confirmation dialog
        val confirmView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bookmark_deletion_confirmation, null)
        val confirmDialog = AlertDialog.Builder(requireContext())
            .setView(confirmView)
            .create()

        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val messageTextView = confirmView.findViewById<TextView>(R.id.deletionMessage)
        val cancelButton = confirmView.findViewById<AppCompatButton>(R.id.cancelButton)
        val deleteButtonConfirm = confirmView.findViewById<AppCompatButton>(R.id.confirmDeleteButton)

        // Build with a Spannable and append a zero-width space to ensure final glyph is included in span
        val titleLine = bookmark.bookmarkTitle + "\u200B" 
        val builder = SpannableStringBuilder()
        builder.append("Delete Bookmark?")
        builder.append('\n')
        val start = builder.length
        builder.append(titleLine)
        val end = builder.length
        val color = ContextCompat.getColor(requireContext(), R.color.reddish)
        builder.setSpan(
            ForegroundColorSpan(color),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        messageTextView.setText(builder, TextView.BufferType.SPANNABLE)

        // When cancel is clicked, restore the edit dialog and dismiss confirmation
        cancelButton.setOnClickListener { 
            confirmDialog.dismiss()
            onEditDialogShow()
        }

        deleteButtonConfirm.setOnClickListener {
            viewModel.deleteBookmark(bookmark)
            // Close confirmation dialog and properly dismiss edit dialog
            confirmDialog.dismiss()
            onEditDialogDismiss()
            // Show reusable confirmation card overlay
            showBookmarkDeletedCard(bookmark.bookmarkTitle)
            // Reset bookmark entity
            bookmarkEntity = BookmarkEntity()
        }

        confirmDialog.show()
    }

    private fun showBookmarkDeletedCard(bookmarkTitle: String) {
        val container = view?.findViewById<RelativeLayout>(R.id.delete_card_container)
        val textView = view?.findViewById<TextView>(R.id.delete_card_text)
        if (container != null && textView != null) {
            val fullText = "Bookmark deleted\n$bookmarkTitle"
            val spannable = SpannableString(fullText)
            val start = "Bookmark deleted\n".length
            val end = start + bookmarkTitle.length
            val color = ContextCompat.getColor(requireContext(), R.color.reddish)
            spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannable
            container.visibility = View.VISIBLE
            container.postDelayed({
                container.visibility = View.GONE
            }, 3000)
        }
    }

    private fun showThankYouDialog() = Dialog(requireContext()).dialog().apply {
        setContentView(R.layout.dialog_thankyou_note)
        val visitButton = findViewById<AppCompatButton>(R.id.visit_button)
        val dismissButton = findViewById<AppCompatButton>(R.id.dismiss_button)
        
        visitButton.setOnClickListener {
            dismiss()
            // Navigate to settings page
            findNavController().navigate(R.id.settingsFragment)
        }
        
        dismissButton.setOnClickListener {
            dismiss()
        }
        
        safeDialogShow()
    }

    private fun setupFloatingButton(isChart: Boolean) {
        val floatingButton = bind.floatingNotesButton
        val notesCountText = bind.floatingNotesCount
        
        // Hide by default
        floatingButton.visibility = View.GONE
        notesCountText.visibility = View.GONE

        if (isChart) {
            val layoutParams = floatingButton.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            layoutParams.topMargin = (24 * resources.displayMetrics.density).toInt()
            floatingButton.layoutParams = layoutParams
        }

        // Observe note count and update floating button display
        viewModel.getNote(id).observe(viewLifecycleOwner) { notes ->
            val noteCount = notes.size
            // Show the floating notes button only when there is at least one note.
            if (noteCount > 0) {
                floatingButton.visibility = View.VISIBLE
                notesCountText.text = "($noteCount)"
                notesCountText.visibility = View.VISIBLE
            } else {
                floatingButton.visibility = View.GONE
                notesCountText.visibility = View.GONE
            }
        }

        floatingButton.setOnClickListener {
            showNotesBottomModal()
        }
    }

    private fun showNotesBottomModal() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_modal_notes, null)
        
        val notesRecyclerView = view.findViewById<RecyclerView>(R.id.modal_notes_recycler)
        val notesTitle = view.findViewById<TextView>(R.id.notes_title)
        
        // Setup swipe-to-delete functionality
        val swipeHandler = object : SwipeDecoratorCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = faNoteAdapter.currentList[position]
                viewModel.deleteNote(note)
                onDeleteNoteSnackbar(note)
            }
        }
        
        // Setup RecyclerView
        notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = faNoteAdapter
        }
        
        // Attach swipe handler to modal RecyclerView
        ItemTouchHelper(swipeHandler).also {
            it.attachToRecyclerView(notesRecyclerView)
        }
        
        // Update title with note count
        viewModel.getNote(id).observe(viewLifecycleOwner) { notes ->
            notesTitle.text = "Notes (${notes.size})"
        }
        
        bottomSheetDialog.setContentView(view)
        
        // bottom sheet background
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = null
            
            // make the dialog window background transparent
            bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        
        bottomSheetDialog.show()
    }

    private fun sendNoteToPendo(noteBody: String) {
        try {
            // Track user note-feedback submission to Pendo with relevant context
            val properties = hashMapOf<String, Any>()
            properties["feedback_text"] = noteBody
            properties["chapter_id"] = id
            properties["chapter_title"] = title
            properties["subchapter_id"] = subChapterEntity.subChapterId
            properties["timestamp"] = System.currentTimeMillis()
            
            Pendo.track("user_feedback_submitted", properties)
            
            Log.d("PendoFeedback", "Note feedback sent to Pendo: $noteBody")
        } catch (e: Exception) {
            Log.e("PendoFeedback", "Failed to send feedback to Pendo", e)
        }
    }
}