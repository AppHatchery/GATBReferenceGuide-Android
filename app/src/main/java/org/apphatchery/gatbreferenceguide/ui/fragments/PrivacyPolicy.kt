package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentPrivacyPolicyBinding
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.utils.EXTENSION
import org.apphatchery.gatbreferenceguide.utils.PAGES_DIR

class PrivacyPolicy : BaseFragment(R.layout.fragment_privacy_policy) {

    private lateinit var bind: FragmentPrivacyPolicyBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentPrivacyPolicyBinding.bind(view)
        setupWebView()
        val baseURL = "file://" + requireContext().cacheDir.toString() + "/"

        bind.bodyWebView.loadUrl(baseURL + PAGES_DIR + "privacy_policy" + EXTENSION)
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