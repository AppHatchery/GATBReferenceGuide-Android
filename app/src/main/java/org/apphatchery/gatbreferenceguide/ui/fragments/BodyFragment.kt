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
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
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
import org.apphatchery.gatbreferenceguide.ui.viewmodels.MainActivityViewModel
import org.apphatchery.gatbreferenceguide.utils.*
import org.apphatchery.gatbreferenceguide.utils.ANALYTICS_BOOKMARK_EVENT
import org.apphatchery.gatbreferenceguide.utils.ANALYTICS_PAGE_EVENT
import org.apphatchery.gatbreferenceguide.utils.EXTENSION
import org.apphatchery.gatbreferenceguide.utils.NOTE_COLOR
import org.apphatchery.gatbreferenceguide.utils.PAGES_DIR
import org.apphatchery.gatbreferenceguide.utils.alertDialog
import org.apphatchery.gatbreferenceguide.utils.dialog
import org.apphatchery.gatbreferenceguide.utils.getActionBar
import org.apphatchery.gatbreferenceguide.utils.getBottomNavigationView
import org.apphatchery.gatbreferenceguide.utils.isChecked
import org.apphatchery.gatbreferenceguide.utils.observeOnce
import org.apphatchery.gatbreferenceguide.utils.safeDialogShow
import org.apphatchery.gatbreferenceguide.utils.searchState
import org.apphatchery.gatbreferenceguide.utils.snackBar
import org.apphatchery.gatbreferenceguide.utils.toast
import sdk.pendo.io.Pendo
import javax.inject.Inject
import kotlin.math.log
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.content.ClipboardManager
import androidx.core.content.ContextCompat
import android.text.TextWatcher
import android.text.Editable
import androidx.activity.OnBackPressedCallback
import android.content.ClipData
import android.widget.RelativeLayout

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

    private fun onDeleteNoteSnackbar(note: NoteEntity) =
        bind.root.snackBar(getString(R.string.note_deleted)).also {
            it.setAction(getString(R.string.undo)) {
                viewModel.insertNote(note)
            }
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
        bind.lastUpdateTextView.text = getString(R.string.last_updated, subChapterEntity.lastUpdated)

        // Setup search functionality
        setupSearch()

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
//search query
        if (bodyUrl.searchQuery.isNotEmpty() && !isOnlyWhitespace(bodyUrl.searchQuery)) {
            // Do not display the bottom search-clear container anymore
            bind.searchClearContainer.visibility = View.GONE
        }

        bind.apply {

            bookmarkImageButton.setOnClickListener { onBookmarkListener() }

            if (chartAndSubChapter != null) isChartView() else {

                val originalTitle = chapterEntity.chapterTitle
                val searchedWordToColor = bodyUrl.searchQuery
                val spannableString = SpannableString(originalTitle)
                val startIndex = originalTitle.indexOf(searchedWordToColor)
                if (startIndex != -1) {
                    val endIndex = startIndex + searchedWordToColor.length
                    val backgroundColorSpan = BackgroundColorSpan(Color.YELLOW)
                    spannableString.setSpan(
                        backgroundColorSpan,
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val foregroundColorSpan = ForegroundColorSpan(Color.BLACK)
                    spannableString.setSpan(
                        foregroundColorSpan,
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                textviewSubChapter.text = spannableString
                val loadUrl = baseURL + PAGES_DIR + subChapterEntity.url + EXTENSION
                val fileFromDir = filesURL + subChapterEntity.url + EXTENSION
                val myURL = "file://${requireContext().filesDir.absolutePath}/${subChapterEntity.url}$EXTENSION"
                if(subChapterEntity.url == "15_appendix_district_tb_coordinators_(by_district)"){
                    textviewSubChapter.visibility = View.GONE
                    lastUpdateTextView.visibility = View.GONE
                    bodyWebView.loadUrl(fileFromDir)
                }else{
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

            homeButton.setOnClickListener {
                findNavController().popBackStack(R.id.mainFragment, false)
            }

            changeFontSizeButton.setOnClickListener {
                showFontDialog()
            }




            fun expandRecyclerView(recyclerView: RecyclerView) {
                recyclerView.measure(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val targetHeight = recyclerView.measuredHeight

                recyclerView.layoutParams.height = 0
                recyclerView.visibility = View.VISIBLE

                val animator = ValueAnimator.ofInt(0, targetHeight).apply {
                    duration = 300 // Adjust duration as needed
                    addUpdateListener { animation ->
                        recyclerView.layoutParams.height = animation.animatedValue as Int
                        recyclerView.requestLayout()
                    }
                }
                animator.start()
            }

            fun collapseRecyclerView(recyclerView: RecyclerView) {
                val initialHeight = recyclerView.measuredHeight

                val animator = ValueAnimator.ofInt(initialHeight, 0).apply {
                    duration = 300
                    addUpdateListener { animation ->
                        recyclerView.layoutParams.height = animation.animatedValue as Int
                        recyclerView.requestLayout()
                    }
                    doOnEnd { recyclerView.visibility = View.GONE }
                }
                animator.start()
            }

            fun toggleRecyclerViewVisibility(recyclerView: RecyclerView, toggleButton: ImageView, isCollapsed: Boolean) {
                if (isCollapsed) {
                    expandRecyclerView(recyclerView)
                    toggleButton.setImageResource(R.drawable.ic_baseline_arrow_up)
                } else {
                    collapseRecyclerView(recyclerView)
                    toggleButton.setImageResource(R.drawable.ic_baseline_arrow_down)
                }
            }
            collapseActionButton.setOnClickListener {
                toggleRecyclerViewVisibility(bind.recyclerviewNote, collapseActionButton, isCollapsed)
                isCollapsed = !isCollapsed
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
            R.drawable.ic_baseline_charts_2,
            0,
            0,
            0
        )


        bookmarkType = BookmarkType.CHART
        textviewSubChapter.text = chartAndSubChapter!!.chartEntity.chartTitle
// here is where the chart webview is being loaded
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
                showNoteDeletionConfirmationPopup {
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
                onSaveBookmark(bookTitleTextInputEditText.text.toString().trim())
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

        showBookmarkSuccessPopup()
    }

    private fun showBookmarkSuccessPopup() {
        Dialog(requireContext()).dialog().apply {
            setContentView(R.layout.dialog_bookmark_success)
            
            val bookmarkedText = findViewById<TextView>(R.id.bookmarked_text)
            val visitButton = findViewById<AppCompatButton>(R.id.visit_button)
            val dismissButton = findViewById<AppCompatButton>(R.id.dismiss_button)
            
            // Set the text with HTML formatting to make "My Bookmarks" and "Home" blue
            bookmarkedText.text = HtmlCompat.fromHtml(
                getString(R.string.bookmarked_message), 
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            
            visitButton.setOnClickListener {
                dismiss()
                // Navigate to bookmarks/home page
                findNavController().popBackStack(R.id.mainFragment, false)
            }
            
            dismissButton.setOnClickListener {
                dismiss()
            }
            
            safeDialogShow()
        }
    }

    private fun showNoteDeletionConfirmationPopup(onConfirm: () -> Unit) {
        Dialog(requireContext()).dialog().apply {
            setContentView(R.layout.dialog_note_deletion_confirmation)

            val yesButton = findViewById<AppCompatButton>(R.id.cancelButton)
            val cancelButton = findViewById<AppCompatButton>(R.id.yesButton)

            yesButton.setOnClickListener {
                dismiss()
                onConfirm()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            safeDialogShow()
        }
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

    var urlGlobal: String? = null

    private fun setupWebView() = bind.bodyWebView.apply {
        onZoomOut()
        
        // Add JavaScript interface for search results
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
            // This is the old system share sheet behavior
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
        }

        searchPrevious.setOnClickListener { navigatePrevious() }
        searchNext.setOnClickListener { navigateNext() }
    }

    private fun expandSearchView() {
        if (isExpanded) return
        isExpanded = true

        // Animate EditText width to 213dp
        val targetWidth = (213 * resources.displayMetrics.density).toInt()

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
        searchCounter.visibility = View.VISIBLE
        searchPrevious.visibility = View.VISIBLE
        searchNext.visibility = View.VISIBLE
    }

    private fun showEmptySearchState() {
        totalMatches = 0
        currentMatch = 0
        searchCounter.text = "0/0"
        searchCounter.visibility = View.VISIBLE
        searchPrevious.visibility = View.VISIBLE
        searchNext.visibility = View.VISIBLE
        searchClear.visibility = View.VISIBLE
        Log.d("BodyFragment", "Showing empty search state")
    }

    private fun collapseSearchView() {
        if (!isExpanded) return
        isExpanded = false

        // Hide controls first
        searchCounter.visibility = View.GONE
        searchPrevious.visibility = View.GONE
        searchNext.visibility = View.GONE
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
                searchEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.frame_16)
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
            // Do not show bottom search-clear container during search
            bind.searchClearContainer.visibility = View.GONE
            
            Log.d("BodyFragment", "Performing search for: $searchQuery")
        }
    }

    private fun updateSearchCount(query: String) {
        // Use JavaScript to count actual occurrences in the WebView content
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
        
        searchCounter.visibility = View.VISIBLE
        searchPrevious.visibility = View.VISIBLE
        searchNext.visibility = View.VISIBLE
        searchClear.visibility = View.VISIBLE
        
        Log.d("BodyFragment", "Updated search UI: ${currentMatch}/${count}")
    }

    private fun navigatePrevious() {
        if (totalMatches > 0) {
            // Navigate to previous match in WebView
            bind.bodyWebView.findNext(false) // false = previous
            // The listener will update currentMatch automatically
        }
    }

    private fun navigateNext() {
        if (totalMatches > 0) {
            // Navigate to next match in WebView
            bind.bodyWebView.findNext(true) // true = next
            // The listener will update currentMatch automatically
        }
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
}