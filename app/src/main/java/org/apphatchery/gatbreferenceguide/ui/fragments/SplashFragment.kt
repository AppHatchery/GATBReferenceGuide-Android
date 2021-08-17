package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentSplashBinding
import org.apphatchery.gatbreferenceguide.db.entities.ChapterEntity
import org.apphatchery.gatbreferenceguide.db.entities.ChartEntity
import org.apphatchery.gatbreferenceguide.db.entities.SubChapterEntity
import org.apphatchery.gatbreferenceguide.resource.Resource
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASplashViewModel
import org.apphatchery.gatbreferenceguide.utils.createHtmlAndAssetsDirectoryIfNotExists
import org.apphatchery.gatbreferenceguide.utils.prepHtmlPlusAssets
import org.apphatchery.gatbreferenceguide.utils.readJsonFromAssetToString

@AndroidEntryPoint
class SplashFragment : BaseFragment(R.layout.fragment_splash) {

    private val viewModel: FASplashViewModel by viewModels()
    private lateinit var fragmentSplashBinding: FragmentSplashBinding


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
                            dumpChapterInfo()
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
                            findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                        }
                        else -> {
                        }
                    }
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentSplashBinding = FragmentSplashBinding.bind(view)

        requireActivity().apply {
            findViewById<View>(R.id.bottomNavigationView).visibility = View.GONE
            createHtmlAndAssetsDirectoryIfNotExists()
            prepHtmlPlusAssets()
            dumpChartData()
        }

    }


}