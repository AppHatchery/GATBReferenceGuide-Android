// Custom WebView that pre-applies all TB guide rendering requirements in one place,
// so every BodyFragment/ChartFragment instance gets consistent behaviour without
// repeating setup code.
//
// Key setup performed in init{}:
//   • JavaScript enabled  — required by UIkit components in the guide HTML.
//   • File/content access — allows loading from cacheDir via file:// URLs.
//   • Zoom controls       — pinch-zoom on; overlay buttons hidden (displayZoomControls=false).
//   • LOAD_NO_CACHE       — always reads from cacheDir, not the WebView HTTP cache, so
//                           content updates are reflected immediately after a re-copy.
//   • Force-dark          — honours Android night-mode for pages that don't ship their own
//                           dark CSS; uses the deprecated WebSettingsCompat API because the
//                           replacement (algorithmic darkening) is not available on API <29.
//   • Find listener       — bridges WebView.findAllAsync() results to [setOnSearchResultListener]
//                           so ExpandableSearchWidget can display "2 / 5" match counters.
//
// applyFontSize() reads the user's font preference (R.string.font_key, set in SettingsFragment)
// and maps 0/1/2/3 to WebSettings.TextSize values.
// Related: BodyFragment (primary host), ExpandableSearchWidget.kt, UserPrefs/SettingsFragment.
package org.apphatchery.gatbreferenceguide.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Canvas
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.preference.PreferenceManager
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import org.apphatchery.gatbreferenceguide.R

@SuppressLint("SetJavaScriptEnabled")
class BaseWebView(context: Context, attributeSet: AttributeSet?) : WebView(context, attributeSet) {

    private lateinit var preferenceManager: SharedPreferences
    private var searchResultListener: ((Int, Int) -> Unit)? = null
    
    fun setOnSearchResultListener(listener: (totalMatches: Int, currentMatch: Int) -> Unit) {
        searchResultListener = listener
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    init {
        with(settings) {
            allowContentAccess = true
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            javaScriptEnabled = true

            preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)

            if (WebViewFeature
                    .isFeatureSupported(WebViewFeature.FORCE_DARK)
            ) {

                when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        WebSettingsCompat.setForceDark(
                            this,
                            WebSettingsCompat.FORCE_DARK_ON
                        )
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        WebSettingsCompat.setForceDark(
                            this,
                            WebSettingsCompat.FORCE_DARK_OFF
                        )
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        WebSettingsCompat.setForceDark(
                            this,
                            WebSettingsCompat.FORCE_DARK_OFF
                        )
                    }
                }

            }

        }
        applyFontSize()
        
        // Override the find listener to capture search results
        setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
            if (isDoneCounting) {
                searchResultListener?.invoke(numberOfMatches, activeMatchOrdinal + 1)
            }
        }
    }


    /**
     * Resets the page scale to 1 and enables overview mode so wide tables fit within the
     * viewport. Called by BodyFragment when the user taps the "zoom out" toolbar button.
     */
    fun onZoomOut() {
        setInitialScale(1)
        with(settings) {
            loadWithOverviewMode = true
            useWideViewPort = true
        }
    }

    private fun applyFontSize() {
        (PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.font_key), 1.toString()).toString()).apply {
                settings.textSize = when (this.toInt()) {
                    0 -> WebSettings.TextSize.SMALLER
                    2 -> WebSettings.TextSize.LARGER
                    3 -> WebSettings.TextSize.LARGEST
                    else -> WebSettings.TextSize.NORMAL
                }
            }

    }
}