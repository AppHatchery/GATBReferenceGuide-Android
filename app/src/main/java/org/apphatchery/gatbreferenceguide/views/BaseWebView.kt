package org.apphatchery.gatbreferenceguide.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.preference.PreferenceManager
import org.apphatchery.gatbreferenceguide.R

@SuppressLint("SetJavaScriptEnabled")
class BaseWebView(context: Context, attributeSet: AttributeSet?) : WebView(context, attributeSet) {

    override fun onDraw(canvas: Canvas?) {
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