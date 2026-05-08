package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.dynamiclinks.dynamicLinks
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentMainBinding
import org.apphatchery.gatbreferenceguide.db.data.ChartAndSubChapter
import org.apphatchery.gatbreferenceguide.db.entities.*
import org.apphatchery.gatbreferenceguide.prefs.UserPrefs
import org.apphatchery.gatbreferenceguide.resource.Resource
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChapterAdapter
import org.apphatchery.gatbreferenceguide.ui.adapters.FAMainFirst6ChartAdapter
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FAMainViewModel
import org.apphatchery.gatbreferenceguide.utils.*
import org.apphatchery.gatbreferenceguide.utils.navigateSafe
import sdk.pendo.io.Pendo
import java.util.UUID
import javax.inject.Inject

import org.apphatchery.gatbreferenceguide.db.Database


private const val BUILD_VERSION = 16
private const val PENDO_RELEASE_VERSION = "Apr-26-"

@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main) {

    private lateinit var fragmentMainBinding: FragmentMainBinding
    private lateinit var first6ChapterAdapter: FAMainFirst6ChapterAdapter
    private lateinit var first6ChartAdapter: FAMainFirst6ChartAdapter
    private val htmlInfoEntity = ArrayList<HtmlInfoEntity>()
    private val viewModel: FAMainViewModel by viewModels()
    private var visitor_id: String? = null
    private lateinit var navController: NavController
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var progressBar : ProgressBar? = null
    private var i = 0
    private val handler = Handler()
    private var initUpdateValue = 0

    private var chartTitleOverrides: Map<String, String> = emptyMap()
    
    // Flags to track initialization state and prevent navigation during seeding
    private var isInitializing = false
    private var initializationComplete = false

    @Inject
    lateinit var userPrefs: UserPrefs

    @Inject
    lateinit var db: Database

    companion object {
         const val VISITOR_ID = ""
         const val ACCOUNT_ID = "GTRG"
//       const val ACCOUNT_ID = "Test"
    }
    private fun setupPendo() = Pendo.startSession(
        visitor_id,
        ACCOUNT_ID,
        null,
        null
    )

    private fun init() {

        lifecycleScope.launch {
            // Web content is stored under cacheDir; if user cleared cache it may be missing.
            // Rebuild it even when BUILD_VERSION has not changed.
            withContext(Dispatchers.IO) {
                AppInitLock.mutex.withLock {
                    val ctx = requireContext().applicationContext
                    if (!ctx.isGuideWebContentPresent()) {
                        ctx.replaceBundledGuideWebContent()
                    }
                }
            }

            visitor_id = getVisitorId()

            with(fragmentMainBinding) {
                progressBar.isVisible = false
                group.isVisible = true
            }
            searchState.exitSearchMode()
            requireActivity().getBottomNavigationView()?.toggleVisibility(true)
            setupPendo()

            first6ChapterAdapter = FAMainFirst6ChapterAdapter().also { adapter ->
                viewModel.getChapter.observe(viewLifecycleOwner) {
                    if (it.size < 16) {
                        // Data not ready yet; keep loading state.
                        fragmentMainBinding.progressBar.isVisible = true
                        fragmentMainBinding.group.isVisible = false
                        return@observe
                    }

                    // IMPORTANT: never submit a mutable list that will be reused/mutated later.
                    // DiffUtil runs asynchronously; if the same list instance is cleared/changed,
                    // AsyncListDiffer can crash with IndexOutOfBoundsException.
                    val predefinedChapterList = arrayListOf(
                        it[0].copy(chapterTitle = "All Chapters>"),
                        it[4].copy(chapterTitle = "Diagnosis of Active TB"),
                        it[5].copy(chapterTitle = "Treatment of Active TB"),
                        it[2].copy(chapterTitle = "Diagnosis of LTBI"),
                        it[3].copy(chapterTitle = "Treatment of LTBI"),
                        it[15].copy(chapterTitle = "District TB Coordinators"),
                    )
                    adapter.submitList(predefinedChapterList)
                }

                adapter.itemClickCallback { chapterEntity ->
                    if (chapterEntity.chapterTitle == "All Chapters>") {
                        findNavController().navigateSafe(R.id.action_mainFragment_to_chapterFragment)
                    } else {
                        MainFragmentDirections.actionMainFragmentToSubChapterFragment(chapterEntity)
                            .apply {
                                findNavController().navigateSafe(this)
                            }
                    }
                }
            }

            viewModel.downloadAndSavePage(
                "https://apphatchery.github.io/GA-TB-Reference-Guide-Web/pages/15_appendix_district_tb_coordinators_(by_district).html",
                requireContext()
            )

            first6ChartAdapter = FAMainFirst6ChartAdapter().also { adapter ->
                viewModel.getChart.observe(viewLifecycleOwner) { data ->
                    if (data.size < 18) {
                        // Data not ready yet; keep loading state.
                        fragmentMainBinding.progressBar.isVisible = true
                        fragmentMainBinding.group.isVisible = false
                        return@observe
                    }

                    chartTitleOverrides = mapOf(
                        data[0].chartEntity.id to "All Tables>",
                        data[7].chartEntity.id to "First Line TB Drugs for Adults",
                        data[10].chartEntity.id to "IV Therapy Drugs",
                        data[11].chartEntity.id to "Alternative Regimens",
                        data[4].chartEntity.id to "Dosages for LTBI Regimens",
                        data[15].chartEntity.id to "Treatment of Extra- pulmonary TB",
                        data[16].chartEntity.id to "TB drugs in Special Situations"
                    )
                    adapter.setTitleOverrides(chartTitleOverrides)

                    // Submit a fresh list instance each time (avoid mutable reuse).
                    val predefinedChartList = listOf(
                        data[0],
                        data[7],
                        data[10],
                        data[11],
                        data[4],
                        data[16],
                        data[15],
                    )
                    adapter.submitList(predefinedChartList)
                }

                adapter.itemClickCallback { chartAndSubChapter ->
                    val displayTitle =
                        chartTitleOverrides[chartAndSubChapter.chartEntity.id]
                            ?: chartAndSubChapter.chartEntity.chartTitle

                    if (displayTitle == "All Tables>" || displayTitle == "All Charts>") {
                        findNavController().navigateSafe(R.id.action_mainFragment_to_chartFragment)
                    } else {
                        viewModel.getChapterInfo(chartAndSubChapter.subChapterEntity.chapterId)
                            .observe(viewLifecycleOwner) { chapterEntity ->
                                MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                                    BodyUrl(chapterEntity, chartAndSubChapter.subChapterEntity, ""),
                                    chartAndSubChapter
                                ).apply {
                                    findNavController().navigateSafe(this)
                                }
                            }
                    }
                }
            }



            fragmentMainBinding.apply {
                recyclerviewFirst6Chapters.setupAdapter(first6ChapterAdapter)
                recyclerviewFirst6Charts.setupAdapter(first6ChartAdapter, 1)


            }

            setupDynamicLink()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fragmentMainBinding = FragmentMainBinding.bind(view)
        viewLifecycleOwner.lifecycleScope.launch {
            val version = userPrefs.getBuildVersion.first()
            if (version != BUILD_VERSION) {
                firstLaunch()
            } else {
                init()
            }
        }
        fragmentMainBinding.bookmark.setOnClickListener {
            findNavController().navigateSafe(MainFragmentDirections.actionMainFragmentToSavedFragment())
        }


        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
                remoteConfig.addOnConfigUpdateListener(object: ConfigUpdateListener{
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains("update_value")) {
                    remoteConfig.activate().addOnCompleteListener {
                        val fetchedValue = remoteConfig.getLong("update_value").toInt()
                        val savedValue = userPrefs.getSavedUpdateValue()
                        if (savedValue != fetchedValue && !userPrefs.isFirstLaunch) {
                            fragmentMainBinding.popupContainer.visibility = View.VISIBLE
                            //circular progress bar
                            fragmentMainBinding.popupDownloadButton.setOnClickListener {
                                fragmentMainBinding.popupContainer.visibility = View.GONE
                                fragmentMainBinding.pbar.isVisible = true
                                viewModel.checkAndUpdatePage(
                                    "https://apphatchery.github.io/GA-TB-Reference-Guide-Web/pages/15_appendix_district_tb_coordinators_(by_district).html",
                                    requireContext(),
                                    "15_appendix_district_tb_coordinators_(by_district).html"
                                )
                                handler.postDelayed({
                                    fragmentMainBinding.pbar.isVisible = false
                                    Toast.makeText(requireContext(), "Download Complete", Toast.LENGTH_SHORT).show()
                                }, 1000)
                            }
                            val properties = hashMapOf<String, Any>()
                            properties["updated"] = fetchedValue
                            Pendo.track("Value Updated", properties)
                        }

                        userPrefs.isFirstLaunch = false
                        userPrefs.saveUpdateValue(fetchedValue)
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
            }

        })

    }


    private fun RecyclerView.setupAdapter(
        listAdapter: RecyclerView.Adapter<*>,
        spanCount: Int = 2,
    ) {
        layoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter = listAdapter
    }

    private fun setupDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(requireActivity().intent)
            .addOnSuccessListener(requireActivity()) { pendingDynamicLink ->
                if (pendingDynamicLink != null)
                    pendingDynamicLink.link?.let {
                        val androidQueryId = it.getQueryParameter("androidQueryId")
                        val androidIsPage = it.getQueryParameter("androidIsPage")
                        if (androidIsPage != null && androidQueryId != null) {
                            requireActivity().intent.data = null
                            requireActivity().intent.replaceExtras(Bundle())
                            handleDynamicLink(androidIsPage.toInt(), androidQueryId)
                        }
                    }
            }
    }

    private fun handleDynamicLink(isPage: Int, id: String) {
        if (isPage == 0) {
            viewModel.getChartAndSubChapterById(id)
                .observe(viewLifecycleOwner) { chartAndSubchapter ->
                    viewModel.getSubChapterInfo(chartAndSubchapter.subChapterEntity.subChapterId.toString())
                        .observe(viewLifecycleOwner) { subChapterEntity ->
                            viewModel.getChapterInfo(subChapterEntity.chapterId)
                                .observe(viewLifecycleOwner) { chapterEntity ->
                                    MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                                        BodyUrl(chapterEntity, subChapterEntity, ""), chartAndSubchapter
                                    ).apply {
                                        findNavController().navigateSafe(this)
                                    }
                                }
                        }
                }
        } else {
            viewModel.getSubChapterInfo(id)
                .observe(viewLifecycleOwner) { subChapterEntity ->
                    viewModel.getChapterInfo(subChapterEntity.chapterId)
                        .observe(viewLifecycleOwner) { chapterEntity ->
                            MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                                BodyUrl(chapterEntity, subChapterEntity,""), null
                            ).apply {
                                findNavController().navigateSafe(this)
                            }
                        }
                }

        }
    }


    private fun Context.dumpHTMLInfo() = assets.apply {
        list(PAGES_DIR.removeSlash())?.forEach {
            val file = PAGES_DIR + it
            var fileName = file.replace(EXTENSION, "")
            fileName = fileName.replace(PAGES_DIR, "")
            htmlInfoEntity.add(
                HtmlInfoEntity(
                    fileName,
                    html2text(file).replace("GA TB Reference Guide", "")
                )
            )
        }
        viewModel.dumpHTMLInfo(htmlInfoEntity)
    }


    private fun Context.dumpChartData() {
        val ofType = object : TypeToken<List<ChartEntity>>() {}.type
        (Gson().fromJson(
            readJsonFromAssetToString("chart.json")!!,
            ofType
        ) as List<ChartEntity>).also {

            viewModel.dumpChartData(it)
                .observe(viewLifecycleOwner) { resource ->
                    when (resource) {
                        is Resource.Success<*> -> {

                            if (viewModel.dumpChartDataObserve) {
                                dumpChapterInfo()
                                viewModel.dumpChartDataObserve = false
                            }

                        }
                        else -> {
                        }
                    }
                }
        }
    }

    private fun Context.dumpChapterInfo() {
        val ofType = object : TypeToken<List<ChapterEntity>>() {}.type
        (Gson().fromJson(
            readJsonFromAssetToString("chapter.json")!!,
            ofType
        ) as List<ChapterEntity>).also {
            viewModel.dumpChapterData(it)
                .observe(viewLifecycleOwner) { resource ->
                    when (resource) {
                        is Resource.Success<*> -> {
                            dumpSubChapterInfo()
                        }
                        else -> {
                        }
                    }
                }
        }
    }

    private fun Context.dumpSubChapterInfo() {
        val ofType = object : TypeToken<List<SubChapterEntity>>() {}.type
        (Gson().fromJson(
            readJsonFromAssetToString("subchapter.json")!!,
            ofType
        ) as List<SubChapterEntity>).also {
            viewModel.dumpSubChapterData(it)
                .observe(viewLifecycleOwner) { resource ->
                    when (resource) {
                        is Resource.Success<*> -> {
                            if (viewModel.dumpSubChapterDataObserver) {
                                dumpHTMLInfo()
                                viewModel.dumpSubChapterDataObserver = false
                            }
                        }
                        else -> {
                        }
                    }
                }
        }
    }


   private fun firstLaunch() {
    requireActivity().apply {
        // Mark initialization as started
        isInitializing = true
        initializationComplete = false

        getBottomNavigationView()?.toggleVisibility(false)
        
        // Keep progress bar visible and hide content during initialization
        fragmentMainBinding.progressBar.isVisible = true
        fragmentMainBinding.group.isVisible = false

        // moved the heavy I/O operations to a background thread to prevent WebView renderer crashes
        // Use Fragment lifecycleScope so this work isn't cancelled on navigation.
        lifecycleScope.launch(Dispatchers.IO) {
            AppInitLock.mutex.withLock {
                // Fully replace cached HTML/CSS/JS/images so app updates never show mixed old/new content.
                applicationContext.replaceBundledGuideWebContent()

                // One-time background migration: update saved notes to point to renamed/merged targets.
                // Example: notes on old tables 10/11/12 -> new table 9.
                LegacyNotesMigrator.migrateNoteTargets(db)

                // Seed DB from bundled assets (purge+insert+global search in one transaction).
                viewModel.purgeAndSeedFromAssets(applicationContext)

                // Persist version only after seeding completes.
                userPrefs.setBuildVersion(BUILD_VERSION)
                userPrefs.setPendoVisitorId(getVisitorId())
            }

            withContext(Dispatchers.Main) {
                // If user navigated away mid-seed, skip touching UI.
                if (view == null) return@withContext

                isInitializing = false
                initializationComplete = true
                requireActivity().getBottomNavigationView()?.isEnabled = true
                init()
            }
        }
    }
}
    private fun generatePendoVisitorId() = PENDO_RELEASE_VERSION + UUID.randomUUID().toString()

    private suspend fun getVisitorId(): String {
        val vID = userPrefs.getPendoVisitorId.first()
        return vID.ifEmpty {
            val id = generatePendoVisitorId()
            userPrefs.setPendoVisitorId(id)
            id
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Prevent navigation during initialization by disabling bottom navigation
        if (isInitializing) {
            requireActivity().getBottomNavigationView()?.isEnabled = false
        } else if (initializationComplete) {
            requireActivity().getBottomNavigationView()?.isEnabled = true
        }
    }
}