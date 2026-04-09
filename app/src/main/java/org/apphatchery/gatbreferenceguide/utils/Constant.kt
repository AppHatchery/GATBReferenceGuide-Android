// Path and naming constants shared across the guide's content pipeline.
//
// The TB guide's offline web content lives in cacheDir with this directory layout:
//   cacheDir/pages/   — HTML article files (one per sub-chapter), extension ".html"
//   cacheDir/assets/  — UIkit CSS/JS and shared stylesheets
//   cacheDir/images/  — drug-table and diagram images referenced from HTML
//   assets/json/      — bundled JSON seed files (chapter.json, subchapter.json, chart.json)
//
// BaseWebView loads files using "file://"+cacheDir paths, so these constants must stay
// consistent with the directory structure in the app's bundled assets folder.
//
// TAG is a catch-all Logcat tag; prefer per-class tags in new code.
// Related: GuideContentUpdater.kt (copies into these dirs), Methods.kt (uses these in I/O helpers),
//          BaseWebView.kt (reads from PAGES_DIR), MainFragment.dumpHTMLInfo().
package org.apphatchery.gatbreferenceguide.utils

const val EXTENSION = ".html"
const val HASH = "#"
const val PAGES_DIR = "pages/"
const val ASSETS_DIR = "assets/"
const val JSON_DIR = "json/"
const val IMAGE_DIR = "images/"
const val TAG = "TAG"
