package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWebViewBinding
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.utils.EXTENSION
import org.apphatchery.gatbreferenceguide.utils.PAGES_DIR

class About : BaseFragment(R.layout.fragment_web_view) {

    private lateinit var bind: FragmentWebViewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWebViewBinding.bind(view)
        val baseURL = "file://" + requireContext().cacheDir.toString() + "/"
        bind.bodyWebView.loadUrl(baseURL + PAGES_DIR + "about_us" + EXTENSION)
    }
}