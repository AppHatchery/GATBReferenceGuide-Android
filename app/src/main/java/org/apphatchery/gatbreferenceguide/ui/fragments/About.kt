// Fragment displaying the "About Us" informational page for the GA-TB Reference Guide app.
// The user sees a static HTML page rendered in a WebView describing the app's background,
// purpose, and the organizations involved in its development.
//
// Data flow: no ViewModel is used. The HTML file is loaded directly from the app's cache
// directory using a file:// URL constructed from cacheDir + PAGES_DIR + "about_us" + EXTENSION.
// The cache content is seeded on first launch (and on updates) by MainFragment/firstLaunch().
//
// Navigation: reached from SettingsFragment via actionSettingsFragmentToAbout (SafeArgs).
// Related: PrivacyPolicy (same WebView pattern), BaseFragment, PAGES_DIR, EXTENSION.
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