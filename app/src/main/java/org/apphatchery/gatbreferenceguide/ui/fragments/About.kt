package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import androidx.preference.PreferenceManager
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWebViewBinding
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.utils.EXTENSION
import org.apphatchery.gatbreferenceguide.utils.PAGES_DIR

class About : BaseFragment(R.layout.fragment_web_view) {

    private lateinit var bind: FragmentWebViewBinding
    private var fontSize = "1"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWebViewBinding.bind(view)
        setupWebView()
        val baseURL = "file://" + requireContext().cacheDir.toString() + "/"

        bind.bodyWebView.loadUrl(baseURL + PAGES_DIR + "about_us" + EXTENSION)

        fontSize = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString(getString(R.string.font_key), "1").toString()


        bind.bodyWebView.settings.textSize = when (fontSize.toInt()) {
            0 -> WebSettings.TextSize.SMALLER
            2 -> WebSettings.TextSize.LARGER
            3 -> WebSettings.TextSize.LARGEST
            else -> WebSettings.TextSize.NORMAL
        }

    }


    private fun setupWebView() = bind.bodyWebView.apply {
        with(settings) {
            allowContentAccess = true
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        }


    }
}