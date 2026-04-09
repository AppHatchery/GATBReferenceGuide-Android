// Fragment displaying the Georgia TB Privacy Policy as a static HTML page in a WebView.
// The user sees the privacy policy content loaded directly from the app's cache directory
// using a file:// URL: cacheDir + PAGES_DIR + "georgia_tb_privacy_policy" + EXTENSION.
//
// Data flow: no ViewModel. HTML is sourced from cached assets seeded by MainFragment on
// first launch (replaceBundledGuideWebContent). No user interaction beyond scrolling.
//
// Navigation: reached from SettingsFragment via actionSettingsFragmentToPrivacyPolicy (SafeArgs).
// Related: About (same WebView pattern), BaseFragment, PAGES_DIR, EXTENSION.
package org.apphatchery.gatbreferenceguide.ui.fragments

import android.os.Bundle
import android.view.View
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentWebViewBinding
import org.apphatchery.gatbreferenceguide.ui.BaseFragment
import org.apphatchery.gatbreferenceguide.utils.EXTENSION
import org.apphatchery.gatbreferenceguide.utils.PAGES_DIR

class PrivacyPolicy : BaseFragment(R.layout.fragment_web_view) {

    private lateinit var bind: FragmentWebViewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind = FragmentWebViewBinding.bind(view)
        val baseURL = "file://" + requireContext().cacheDir.toString() + "/"
        bind.bodyWebView.loadUrl(baseURL + PAGES_DIR + "georgia_tb_privacy_policy" + EXTENSION)
    }

}