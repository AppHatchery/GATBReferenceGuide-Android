package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentSplashBinding
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.HtmlInfoEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.prefs.UserPrefs
import org.apphatchery.gatbreferenceguide.resource.Resource
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASplashViewModel
import org.apphatchery.gatbreferenceguide.utils.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragment(R.layout.fragment_splash) {

    private val viewModel: FASplashViewModel by viewModels()
    private lateinit var fragmentSplashBinding: FragmentSplashBinding
    private val htmlInfoEntity = ArrayList<HtmlInfoEntity>()

    @Inject
    lateinit var userPrefs: UserPrefs


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


    private fun firstLaunch(view: View) {
        fragmentSplashBinding = FragmentSplashBinding.bind(view)

        requireActivity().apply {
            findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE
            createHtmlAndAssetsDirectoryIfNotExists()
            prepHtmlPlusAssets()
            dumpChartData()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskFlowEvent.collect {

                when (it) {
                    FASplashViewModel.Callback.InsertHTMLInfoComplete -> {
                        viewModel.bindHtmlWithChapter()
                    }
                    FASplashViewModel.Callback.InsertGlobalSearchInfoComplete -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            userPrefs.setFirstLaunch(false)
                        }
                        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userPrefs.getFirstLaunch.asLiveData().observe(viewLifecycleOwner) {
             if (it) firstLaunch(view) else{
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
            }
        }


    }


}