package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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
import sdk.pendo.io.Pendo
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume


private const val BUILD_VERSION = 11
private const val PENDO_RELEASE_VERSION = "Sept-24-"

@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main) {

    private lateinit var fragmentMainBinding: FragmentMainBinding
    private lateinit var first6ChapterAdapter: FAMainFirst6ChapterAdapter
    private lateinit var first6ChartAdapter: FAMainFirst6ChartAdapter
    private lateinit var predefinedChapterList: ArrayList<ChapterEntity>
    private lateinit var predefinedChartList: ArrayList<ChartAndSubChapter>
    private val htmlInfoEntity = ArrayList<HtmlInfoEntity>()
    private val viewModel: FAMainViewModel by viewModels()
    private var visitor_id: String? = null
    private lateinit var navController: NavController
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var progressBar : ProgressBar? = null
    private var i = 0
    private val handler = Handler()
    private var initUpdateValue = 0

    @Inject
    lateinit var userPrefs: UserPrefs

    companion object {
        //const val VISITOR_ID = ""
//        const val ACCOUNT_ID = "GTRG"
        const val ACCOUNT_ID = "Test"
    }
    private fun setupPendo() = Pendo.startSession(
        visitor_id,
        ACCOUNT_ID,
        null,
        null
    )

    private fun init() {

        lifecycleScope.launch {
            visitor_id = getVisitorId()

            with(fragmentMainBinding) {
                progressBar.isVisible = false
                group.isVisible = true
            }
            searchState.exitSearchMode()
            requireActivity().getBottomNavigationView()?.toggleVisibility(true)

            predefinedChapterList = ArrayList()
            predefinedChartList = ArrayList()
            setupPendo()

            first6ChapterAdapter = FAMainFirst6ChapterAdapter().also { adapter ->
                viewModel.getChapter.observe(viewLifecycleOwner) {
                    with(predefinedChapterList) {
                        clear()
                        add(it[0].copy(chapterTitle = "See All Chapters"))
                        add(it[3].copy(chapterTitle = "Diagnosis for Active TB"))
                        add(it[4].copy(chapterTitle = "Treatment for Active TB"))
                        add(it[1].copy(chapterTitle = "Diagnosis for LTBI"))
                        add(it[2].copy(chapterTitle = "Treatment for LTBI"))
                        add(it[14].copy(chapterTitle = "District TB Coordinators"))
                        adapter.submitList(this)
                    }
                }

                adapter.itemClickCallback { chapterEntity ->
                    if (chapterEntity.chapterTitle == "See All Chapters") {
                        findNavController().navigate(R.id.action_mainFragment_to_chapterFragment)
                    } else {
                        MainFragmentDirections.actionMainFragmentToSubChapterFragment(chapterEntity)
                            .apply {
                                findNavController().navigate(this)
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
                    with(predefinedChartList) {
                        clear()
                        add(data[0].copy(chartEntity = data[0].chartEntity.copy(chartTitle = "See All Charts")))
                        add(data[7].copy(chartEntity = data[7].chartEntity.copy(chartTitle = "First Line TB Drugs for Adults")))
                        add(data[13].copy(chartEntity = data[13].chartEntity.copy(chartTitle = "IV Therapy Drugs")))
                        add(data[14].copy(chartEntity = data[14].chartEntity.copy(chartTitle = "Alternative Regimens")))
                        add(data[4].copy(chartEntity = data[4].chartEntity.copy(chartTitle = "Dosages for LTBI Regimens")))
                        add(data[18].copy(chartEntity = data[18].chartEntity.copy(chartTitle = "Treatment of Extra- pulmonary TB")))
                        add(data[19].copy(chartEntity = data[19].chartEntity.copy(chartTitle = "TB drugs in Special Situations")))
                        adapter.submitList(this)
                    }
                }

                adapter.itemClickCallback { chartAndSubChapter ->
                    if (chartAndSubChapter.chartEntity.chartTitle == "See All Charts") {
                        findNavController().navigate(R.id.action_mainFragment_to_chartFragment)
                    } else {
                        viewModel.getChapterInfo(chartAndSubChapter.subChapterEntity.chapterId)
                            .observe(viewLifecycleOwner) { chapterEntity ->
                                MainFragmentDirections.actionMainFragmentToBodyFragmentDirect(
                                    BodyUrl(chapterEntity, chartAndSubChapter.subChapterEntity, ""),
                                    chartAndSubChapter
                                ).apply {
                                    findNavController().navigate(this)
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
        userPrefs.getBuildVersion.asLiveData().observe(viewLifecycleOwner) {
            if (it != BUILD_VERSION)
            {
                firstLaunch()
            }
            else {
                init()
            }
        }
        fragmentMainBinding.bookmark.setOnClickListener {
            MainFragmentDirections.actionMainFragmentToSavedFragment().apply {
                findNavController().navigate(this)
            }
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
                                        findNavController().navigate(this)
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
                                findNavController().navigate(this)
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
                        is Resource.Success -> {

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
                        is Resource.Success -> {
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
                        is Resource.Success -> {
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
            viewModel.purgeData()
            getBottomNavigationView()?.toggleVisibility(false)
            createHtmlAndAssetsDirectoryIfNotExists()
            prepHtmlPlusAssets()
            dumpChartData()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskFlowEvent.collect {

                when (it) {
                    FAMainViewModel.Callback.InsertHTMLInfoComplete -> {
                        viewModel.bindHtmlWithChapter()
                    }
                    FAMainViewModel.Callback.InsertGlobalSearchInfoComplete -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            userPrefs.setBuildVersion(BUILD_VERSION)
                            userPrefs.setPendoVisitorId(getVisitorId())
                        }
                        init()
                    }
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
}