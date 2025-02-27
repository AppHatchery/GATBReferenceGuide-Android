package org.apphatchery.gatbreferenceguide.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentGlobalSearchBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.BodyUrl
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.GlobalSearchEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAGlobalSearchAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FASearchRecentAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAGlobalSearchViewModel
import org.apphatchery.gatbreferenceguide.utils.*
import org.jsoup.Jsoup
import sdk.pendo.io.Pendo
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GlobalSearchFragment : BaseFragment(R.layout.fragment_global_search) {

    private lateinit var bind: FragmentGlobalSearchBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: FAGlobalSearchViewModel by viewModels()
    private lateinit var recentSearchAdapter: FASearchRecentAdapter

    @Inject
    lateinit var faGlobalSearchAdapter: FAGlobalSearchAdapter

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private var currentTab = 0
    private var isLoading = false
    private var lastSearchQuery = ""
    private var isManualTabSelection = false

    private val handler = Handler(Looper.getMainLooper())
    private var scrollRunnable: Runnable? = null

    private var searchJob: Job? = null



    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentGlobalSearchBinding.bind(view)

       currentTab = savedInstanceState?.getInt("currentTab") ?: 0
        setupRecyclerView()
        setupSearchView()
        setupTabLayout()
        observeSearchResults()

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                voiceSearchForActivityResult(it) { searchText ->
                    bind.searchKeyword.onSearchKeyword(searchText)
                }
            }

        setupRecentSearchAdapter()


        bind.recentRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentSearchAdapter
        }

        val isVisible = recentSearchAdapter.itemCount > 0
        bind.recentTitle.visibility = if (isVisible) View.VISIBLE else View.GONE
        bind.recentRecyclerView.visibility = if (isVisible) View.VISIBLE else View.GONE


        faGlobalSearchAdapter.also { faGlobalSearchAdapter ->
            viewModel.getGlobalSearchEntity.observe(viewLifecycleOwner) { word ->
                updateSearchResults(word)

            }

            faGlobalSearchAdapter.itemClickCallback {
                bind.searchKeyword.toggleSoftKeyboard(requireContext(), false)

                val properties = hashMapOf<String, Any>()
                properties["searchKeyword"] = bind.searchKeyword.text.toString()
                properties["SelectedResult"] = it.searchTitle
                Pendo.track("searchResultClicked", properties)

                /*Log search keyword name*/
                firebaseAnalytics.logEvent(
                    ANALYTICS_SEARCH_EVENT,
                    bundleOf(Pair(ANALYTICS_SEARCH_EVENT, bind.searchKeyword.text.toString()))
                )
                val cleanSearchString = if (bind.searchKeyword.text.toString()
                        .isNotEmpty() && bind.searchKeyword.text.toString() != " "
                ) bind.searchKeyword.text.toString() else ""
                viewModel.getSubChapterById(it.subChapterId.toString())
                    .observe(viewLifecycleOwner) { subChapter ->
                        val cleanedTitle = Jsoup.parse(it.searchTitle).text()
                        val chartEntity = ChartEntity(
                            it.chartId,
                            cleanedTitle,
                            it.subChapter,
                            it.subChapterId,
                            0
                        )
                        val chartAndSubChapter = ChartAndSubChapter(chartEntity, subChapter)

                        GlobalSearchFragmentDirections.actionGlobalSearchFragmentToBodyFragment(
                            BodyUrl(
                                ChapterEntity(it.chapterId, it.searchTitle),
                                subChapter,
                                cleanSearchString
                            ), if(it.isChart) chartAndSubChapter else null
                        ).also {
                            findNavController().navigate(it)
                        }
                        bind.searchKeyword.clearFocus()
                    }
                recentSearchAdapter.updateRecentSearches(bind.searchKeyword.text.toString())
            }

        }

        bind.apply {
            regimens.setOnClickListener {
                bind.searchKeyword.onSearchKeyword("Regimens")
                hideKeyboard()
                showLoading()
                hideSuggestedLoading()
            }

            pregnancy.setOnClickListener {
                bind.searchKeyword.onSearchKeyword("Pregnancy")
                hideKeyboard()
                showLoading()
                hideSuggestedLoading()
            }
           rifampin.setOnClickListener {
                bind.searchKeyword.onSearchKeyword("Rifampin")
               hideKeyboard()
               showLoading()
               hideSuggestedLoading()
            }
            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faGlobalSearchAdapter
            }


            clearSearch.setOnClickListener {
                searchKeyword.text?.clear()
                bind.recyclerview.visibility = View.GONE
                bind.suggestedContent.visibility = View.VISIBLE
                bind.tabLayout.visibility = View.GONE
                bind.tabLayoutContainer.visibility = View.GONE
                bind.searchItemCount.visibility = View.GONE
            }

            recyclerview.visibility = View.GONE
            searchProgressBar.visibility = View.GONE


            setupSearchTextWatcher()
            searchKeyword.doAfterTextChanged { editable ->
                if (editable != null) {
                    if (editable.isBlank()) {
                        viewModel.searchQuery.value = ""
                    }
                }
            }


        }

        bind.searchKeyword.apply {
            requestFocus()
            toggleSoftKeyboard(requireContext())
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentTab", currentTab)
    }

    override fun onResume() {
        super.onResume()
        if (!isManualTabSelection) {
            syncTabWithContent()
        }
        isManualTabSelection = false
        scrollRunnable?.let { handler.removeCallbacks(it) }

        scrollRunnable = Runnable { scrollToTop() }
        handler.postDelayed(scrollRunnable!!, 500)
    }

    override fun onDestroyView() {
        val activity = requireActivity()
        val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        scrollRunnable?.let { handler.removeCallbacks(it) }

        super.onDestroyView()
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


    private fun setupRecyclerView() {
        bind.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        bind.recyclerview.adapter = faGlobalSearchAdapter
    }

    private fun setupSearchView() {
        bind.searchKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun syncTabWithContent() {
        val results = viewModel.getGlobalSearchEntity.value ?: emptyList()
        val newTab = when {
            results.isEmpty() -> 0
            results.all { !it.isChart } -> 1
            results.all { it.isChart } -> 2
            else -> 0
        }
        if (newTab != currentTab) {
            currentTab = newTab
            bind.tabLayout.getTabAt(currentTab)?.select()
            scrollToTop()
            filterAndHighlightResults()
        }
    }

    private fun setupTabLayout() {
        val tabTitles = listOf("All", "Chapters", "Charts")
        val tabIcons = listOf(
            null,
            R.drawable.ic_baseline_chapter_3,
            R.drawable.ic_baseline_charts_3
        )

       for (i in tabTitles.indices){
            val customTab = layoutInflater.inflate(R.layout.custom_tab_layout, null)
            val tabIcon = customTab.findViewById<ImageView>(R.id.tab_icon)
            val tabText = customTab.findViewById<TextView>(R.id.tab_text)
           val tabContainer = customTab.findViewById<LinearLayout>(R.id.tab_container)

            tabText.text = tabTitles[i]

            if(tabIcons[i] != null){
                tabIcon.setImageResource(tabIcons[i]!!)
                tabIcon.visibility = View.VISIBLE
            }else{
                tabIcon.visibility = View.GONE
            }

            val tab = bind.tabLayout.newTab().setCustomView(customTab)
            bind.tabLayout.addTab(tab)

            tab.view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

        }

        bind.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                showLoading()
                filterAndHighlightResults()
                updateTabAppearance(tab, true)
                updateUIWithResults(viewModel.getGlobalSearchEntity.value ?: emptyList())
                Handler(Looper.getMainLooper()).postDelayed({
                    updateUIWithResults(viewModel.getGlobalSearchEntity.value ?: emptyList())
                    scrollToTop()
                    hideLoading()
                }, 300)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                updateTabAppearance(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
        updateTabAppearance(bind.tabLayout.getTabAt(0), true)

        for (i in 1 until bind.tabLayout.tabCount) {
            updateTabAppearance(bind.tabLayout.getTabAt(i), false)
        }

    }

    private fun updateTabAppearance(tab: TabLayout.Tab?, isSelected: Boolean){
        //tab?.customView?.findViewById<LinearLayout>(R.id.tab_container)?.isSelected = isSelected
        tab?.customView?.let { customView ->
            val container = customView.findViewById<LinearLayout>(R.id.tab_container)
            val icon = customView.findViewById<ImageView>(R.id.tab_icon)
            val text = customView.findViewById<TextView>(R.id.tab_text)
            container.isSelected = isSelected

            if(container.isSelected){
                icon.imageTintList =   ContextCompat.getColorStateList(requireContext(), R.color.white)
            }else{
                icon.imageTintList =   ContextCompat.getColorStateList(requireContext(), R.color.neutral_600)
            }


        }
    }

    private fun observeSearchResults() {
        viewModel.getGlobalSearchEntity.observe(viewLifecycleOwner) { results ->
            searchJob?.cancel() // Cancel any ongoing search

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    updateSearchResults(results)
                    delay(300)
                    updateUIWithResults(viewModel.getGlobalSearchEntity.value ?: emptyList())
                    hideRecentLoading()
                } catch (e: CancellationException) {
                    throw e
                }
            }
        }
    }

    private fun updateSearchResults(results: List<GlobalSearchEntity>) {
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val highlightedResults = withContext(Dispatchers.IO) {
                    highlightSearchResults(results)
                }
                withContext(Dispatchers.Main) {
                    faGlobalSearchAdapter.updateData(highlightedResults)
                    filterAndHighlightResults()
                    updateUIWithResults(highlightedResults)
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private fun setupRecentSearchAdapter() {
        recentSearchAdapter = FASearchRecentAdapter(requireContext()) { searchText ->
            searchJob?.cancel()
            showLoading()

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                try {
                    bind.searchKeyword.setText(searchText)
                    bind.searchKeyword.onSearchKeyword(searchText)
                    hideKeyboard()
                    delay(100)
                } catch (e: CancellationException) {
                    throw e
                }
            }
        }
    }

    private fun setupSearchTextWatcher() {
        bind.searchKeyword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                handleSearchTextChange(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun handleSearchTextChange(text: String) {
        searchJob?.cancel()
        val trimmedText = text.trim()
        viewModel.searchQuery.value = text
        with(bind.searchItemCount) {
            visibility = if (trimmedText.isBlank()) View.GONE else View.VISIBLE
        }
        updateSearchViewVisibility(trimmedText.isBlank())
    }

    private fun updateSearchViewVisibility(isBlank: Boolean) {
        with(bind) {
            recyclerview.visibility = if (isBlank) View.GONE else View.VISIBLE
            searchProgressBar.visibility = View.GONE
            suggestedContent.visibility = if (isBlank) View.VISIBLE else View.GONE
            tabLayout.visibility = if (isBlank) View.GONE else View.VISIBLE
            tabLayoutContainer.visibility = if (isBlank) View.GONE else View.VISIBLE
        }
    }

    private fun highlightSearchResults(results: List<GlobalSearchEntity>): List<GlobalSearchEntity> {
        val search = viewModel.searchQuery.value ?: ""
        val searchWords = search.split(Regex("[\\s.,]+"))
            .filter { it.isNotEmpty() }

        return results.map { item ->
            val highlightedTextInBody = highlightText(item.textInBody, searchWords)
            val highlightedSearchTitle = highlightText(item.searchTitle, searchWords)
            val highlightedSubChapter = highlightText(item.subChapter, searchWords)

            item.copy(
                subChapter = highlightedSubChapter,
                searchTitle = highlightedSearchTitle,
                textInBody = highlightedTextInBody
            )
        }
    }

    private fun highlightText(original: String, wordsToHighlight: List<String>): String {
        if (wordsToHighlight.isEmpty() || original.isEmpty()) return original
        var result = original
        for (word in wordsToHighlight) {
            if (word.isNotEmpty()) {
                val pattern = word.replace(Regex("[\\s.,]+"), "")
                if (pattern.isNotEmpty()) {
                    val regex = Regex("(?i)($pattern)")
                    result = result.replace(regex) {
                        "<span style='background-color: yellow; color: black; font-weight: bold;'>${it.value}</span>"
                    }
                }
            }
        }
        return result
    }


    private fun filterAndHighlightResults(){
      when (currentTab) {
            0 -> faGlobalSearchAdapter.filter(FAGlobalSearchAdapter.SearchResultType.ALL)
            1 -> faGlobalSearchAdapter.filter(FAGlobalSearchAdapter.SearchResultType.CHAPTERS)
            2 -> faGlobalSearchAdapter.filter(FAGlobalSearchAdapter.SearchResultType.CHARTS)
            else -> faGlobalSearchAdapter.filter(FAGlobalSearchAdapter.SearchResultType.ALL)
        }
    }

    private fun showLoading() {
        isLoading = true
        bind.loadingIndicator.visibility = View.VISIBLE
        bind.recyclerview.visibility = View.GONE
        bind.suggestedContent.visibility = View.GONE
    }

    private fun hideRecentLoading(){
        isLoading = false
        bind.loadingIndicator.visibility = View.GONE
    }

    private fun hideLoading() {
        isLoading = false
        bind.loadingIndicator.visibility = View.GONE
        bind.recyclerview.visibility = View.VISIBLE
    }

    private fun hideSuggestedLoading(){
        Handler(Looper.getMainLooper()).postDelayed({
            updateUIWithResults(viewModel.getGlobalSearchEntity.value ?: emptyList())
            hideLoading()
        }, 300)
    }

    private fun updateUIWithResults(allResults: List<GlobalSearchEntity>) {
       // if (isLoading) return
        bind.searchProgressBar.visibility = View.GONE

        val filteredResults = when (currentTab) {
            0 -> allResults
            1 -> allResults.filter { !it.isChart }
            2 -> allResults.filter { it.isChart }
            else -> allResults
        }


        val count = filteredResults.size

        bind.visibleViewGroup.visibility = if (count > 0) View.VISIBLE else View.GONE
        bind.noItemFound.visibility = if (count == 0 && bind.suggestedContent.visibility == View.GONE) View.VISIBLE else View.GONE

        bind.searchItemCount.text = when (currentTab) {
            0 -> "${count} result${if (count == 1) "" else "s"} in"
            1 -> "${filteredResults.count { !it.isChart }} result${if (count == 1) "" else "s"} in"
            2 -> "${filteredResults.count { it.isChart }} result${if (count == 1) "" else "s"} in"
            else -> "${count} result${if (count == 1) "" else "s"} in"
        }
    }

    private fun scrollToTop() {
        bind.recyclerview.post {
            bind.recyclerview.smoothScrollToPosition(0)
        }
    }

    private fun performSearch() {
        val query = bind.searchKeyword.text.toString().trim()
        if (query.isNotEmpty()) {
            viewModel.searchQuery.value = query
            bind.searchProgressBar.visibility = View.VISIBLE
            viewModel.getGlobalSearchEntity
        }
    }
}



