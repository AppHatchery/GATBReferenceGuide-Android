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
    }


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