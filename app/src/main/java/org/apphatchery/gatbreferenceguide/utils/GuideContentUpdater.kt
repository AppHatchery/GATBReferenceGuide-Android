package org.apphatchery.gatbreferenceguide.utils

import android.content.Context
import java.io.File

/**
 * Guide web content (HTML/CSS/JS/images) is stored under cacheDir.
 * App updates do NOT clear cacheDir, and our copy routine does not overwrite existing files.
 *
 * To avoid mixed old/new files, we delete the cached directories and re-copy from bundled assets.
 *
 * This also repairs the app after the user clears app cache (files are gone, prefs remain).
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

fun Context.isGuideWebContentPresent(): Boolean {
    val pagesDir = File(cacheDir, PAGES_DIR)
    return pagesDir.exists() && (pagesDir.listFiles()?.isNotEmpty() == true)
}
