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
import kotlinx.coroutines.launch
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
import sdk.pendo.io.Pendo
import javax.inject.Inject


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

    private val searchJob = Job()
    private val searchScope = CoroutineScope(Dispatchers.Main + searchJob)

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentGlobalSearchBinding.bind(view)


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

        recentSearchAdapter = FASearchRecentAdapter(requireContext()) { searchText ->
            bind.searchKeyword.onSearchKeyword(searchText)
        }

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
                        val chartEntity = ChartEntity(
                            it.chartId,
                            it.searchTitle,
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
            }

            pregnancy.setOnClickListener {
                bind.searchKeyword.onSearchKeyword("Pregnancy")
            }
           rifampin.setOnClickListener {
                bind.searchKeyword.onSearchKeyword("Rifampin")
            }
            recyclerview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = faGlobalSearchAdapter
            }

            voiceSearch.setOnClickListener {
                voiceSearchListener(resultLauncher)
            }

            recyclerview.visibility = View.GONE
            searchProgressBar.visibility = View.GONE

            searchKeyword.setOnTextWatcher(
                onTextChangedListener = {
                    viewModel.searchQuery.value = it

                    with(bind.searchItemCount) {
                        visibility = if (searchKeyword.text.toString().trim()
                                .isBlank()
                        ) View.GONE else View.VISIBLE
                    }

                    viewModel.searchQuery.value = it.trim()

                    if (searchKeyword.text.toString().trim()
                            .isBlank()) {
                        recyclerview.visibility = View.GONE
                        bind.searchProgressBar.visibility = View.GONE
                        bind.suggestedContent.visibility = View.VISIBLE
                        bind.tabLayout.visibility = View.GONE
                        bind.tabLayoutContainer.visibility = View.GONE
                    } else {
                        bind.recyclerview.visibility = View.VISIBLE
                        bind.suggestedContent.visibility = View.GONE
                        bind.tabLayout.visibility = View.VISIBLE
                        bind.tabLayoutContainer.visibility = View.VISIBLE
                        //bind.searchProgressBar.visibility = View.VISIBLE

                    }
                })
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



    override fun onDestroyView() {
        val activity = requireActivity()
        val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)

        super.onDestroyView()
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
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

        }

        bind.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                //showLoading()
                filterAndHighlightResults()
                updateTabAppearance(tab, true)
                updateUIWithResults(viewModel.getGlobalSearchEntity.value ?: emptyList())
//                Handler(Looper.getMainLooper()).postDelayed({
//                    updateUIWithResults(viewModel.getGlobalSearchEntity.value ?: emptyList())
//                    hideLoading()
//                }, 300)
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
            faGlobalSearchAdapter.updateData(results)
            updateSearchResults(results)
        }
    }

    private fun updateSearchResults(results: List<GlobalSearchEntity>) {
        val highlightedResults = highlightSearchResults(results)
        faGlobalSearchAdapter.updateData(highlightedResults)
        filterAndHighlightResults()
       updateUIWithResults(highlightedResults)
    }

    private fun highlightSearchResults(results: List<GlobalSearchEntity>): List<GlobalSearchEntity> {
        val search = viewModel.searchQuery.value ?: ""
        val searchWords = search.split("\\s+".toRegex())

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
        var result = original
        for (word in wordsToHighlight) {
            val regex = Regex("(?i)\\b${Regex.escape(word)}\\b")
            result = result.replace(regex) {
                "<span style='background-color: yellow; color: black; font-weight: bold;'>${it.value}</span>"
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
    }

    private fun hideLoading() {
        isLoading = false
        bind.loadingIndicator.visibility = View.GONE
        bind.recyclerview.visibility = View.VISIBLE
    }

    private fun updateUIWithResults(allResults: List<GlobalSearchEntity>) {
        //if (isLoading) return
        bind.searchProgressBar.visibility = View.GONE

        val filteredResults = when (currentTab) {
            0 -> allResults
            1 -> allResults.filter { !it.isChart }
            2 -> allResults.filter { it.isChart }
            else -> allResults
        }


        val count = filteredResults.size

        bind.visibleViewGroup.visibility = if (count > 0) View.VISIBLE else View.GONE
        bind.noItemFound.visibility = if (count == 0) View.VISIBLE else View.GONE

        bind.searchItemCount.text = when (currentTab) {
            0 -> "${count} result${if (count == 1) "" else "s"} in"
            1 -> "${filteredResults.count { !it.isChart }} result${if (count == 1) "" else "s"} in"
            2 -> "${filteredResults.count { it.isChart }} result${if (count == 1) "" else "s"} in"
            else -> "${count} result${if (count == 1) "" else "s"} in"
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



