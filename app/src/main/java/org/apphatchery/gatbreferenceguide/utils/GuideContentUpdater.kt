// Manages the copy of bundled HTML/CSS/JS/image guide content into cacheDir for offline use.
//
// WHY cacheDir?: BaseWebView loads articles via "file://"+cacheDir paths. Android's asset://
// scheme is not writable, so content must first be copied to a mutable location. cacheDir
// is used (not filesDir) because losing the cache only forces a re-copy, not data loss.
//
// GOTCHA — stale mixed content: app updates do NOT clear cacheDir automatically. If new HTML
// references updated CSS/images that haven't overwritten the old ones, pages render broken.
// replaceBundledGuideWebContent() solves this by deleting all three directories first, then
// re-copying everything fresh from the APK's bundled assets.
//
// Startup flow (MainFragment):
//   firstLaunch()  → always calls replaceBundledGuideWebContent() (inside AppInitLock)
//   init()         → calls isGuideWebContentPresent(); copies if cache was cleared by user.
// Related: Constant.kt (PAGES_DIR/ASSETS_DIR/IMAGE_DIR), Methods.kt (prepHtmlPlusAssets),
//          AppInitLock.kt, BaseWebView.kt.
package org.apphatchery.gatbreferenceguide.utils

import android.content.Context
import java.io.File

/**
 * Atomically replaces all cached guide web content with the version bundled in this APK.
 * Deletes pages/, assets/, and images/ directories first, then re-copies from bundled assets.
 * Must be called inside [AppInitLock.mutex] to avoid racing with a concurrent WebView load.
 * Called every time firstLaunch() runs (i.e. on each new BUILD_VERSION).
 */
fun Context.replaceBundledGuideWebContent() {
    clearCachedGuideWebContent()
    createHtmlAndAssetsDirectoryIfNotExists()
    prepHtmlPlusAssets()
}

fun Context.clearCachedGuideWebContent() {
    arrayOf(PAGES_DIR, ASSETS_DIR, IMAGE_DIR).forEach { dir ->
        File(cacheDir, dir).deleteRecursively()
    }
}

/**
 * Returns true when the pages/ directory exists and contains at least one HTML file.
 * Used by MainFragment.init() (the non-first-launch path) to detect a user-cleared cache
 * and trigger a re-copy without incrementing BUILD_VERSION.
 */
fun Context.isGuideWebContentPresent(): Boolean {
    val pagesDir = File(cacheDir, PAGES_DIR)
    return pagesDir.exists() && (pagesDir.listFiles()?.isNotEmpty() == true)
}
