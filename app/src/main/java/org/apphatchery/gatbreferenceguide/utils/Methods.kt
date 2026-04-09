// General-purpose Context/View/Dialog extension functions used throughout the app.
//
// Content I/O helpers (used in the startup content-copy pipeline):
//   toInternalStorage()                  — streams an InputStream to a file under cacheDir;
//                                          the `override` flag controls whether to skip existing files.
//   prepHtmlPlusAssets()                 — iterates bundled assets/pages/images directories and
//                                          copies each file to cacheDir; called by GuideContentUpdater.
//   createHtmlAndAssetsDirectoryIfNotExists() — creates the three cacheDir sub-directories.
//   readJsonFromAssetToString()          — reads a JSON seed file from assets/json/; returns null
//                                          on failure so callers can handle missing bundled data.
//   html2text()                          — strips HTML tags via Jsoup; used to build plain-text
//                                          search index rows in MainFragment.dumpHTMLInfo().
//
// UI helpers: Dialog.dialog(), Dialog.safeDialogShow(), Context.toast(), View.snackBar(),
//   View.toggleVisibility(), Int.noItemFound(), Activity.getBottomNavigationView(),
//   BottomNavigationView.isChecked(), Context.alertDialog().
//
// Related: GuideContentUpdater.kt (calls prepHtmlPlusAssets), Constant.kt (path constants),
//          MainFragment (calls readJsonFromAssetToString, html2text, noItemFound).
package org.apphatchery.gatbreferenceguide.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import org.apphatchery.gatbreferenceguide.R
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

fun Dialog.dialog(): Dialog {
    this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)).also { return this }
}

fun Dialog.safeDialogShow() {
    Objects.requireNonNull(window!!).setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    show()
}

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun View.snackBar(msg: String) = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).also {
    it.show()
}


private fun InputStream.writeToDisk(file: File) {
    use {
        FileOutputStream(file).use { output ->
            val buffer = ByteArray(1024)
            var read: Int
            while (it.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
        }
    }

}

/**
 * Writes [inputStream] to [filename] under [cacheDir].
 * If [override] is false the write is skipped when the file already exists —
 * used during the initial content copy to avoid re-copying unchanged assets.
 * Set [override] to true (default) when the file must always be refreshed.
 */
fun Context.toInternalStorage(
    inputStream: InputStream,
    filename: String,
    override: Boolean = true,
) {
    File(cacheDir, filename).also { file ->
        if (override) inputStream.writeToDisk(file) else if (file.exists()
                .not()
        ) inputStream.writeToDisk(file)
    }
}

fun Context.createHtmlAndAssetsDirectoryIfNotExists() {
    arrayOf(ASSETS_DIR, PAGES_DIR, IMAGE_DIR).forEach { dir ->
        File(cacheDir, dir).also {
            if (it.isDirectory.not())
                it.mkdir()
        }
    }
}


fun Context.readJsonFromAssetToString(file: String): String? {
    return try {
        assets.open(JSON_DIR + file).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        null
    }
}

fun String.removeSlash() = replace("/", "")

/**
 * Copies all files from the bundled assets/, pages/, and images/ directories into cacheDir.
 * Uses override=false so files that already exist are skipped (idempotent on re-entry).
 * Called by [replaceBundledGuideWebContent] after clearing the target directories.
 */
fun Context.prepHtmlPlusAssets(): AssetManager = assets.apply {
    list(ASSETS_DIR.removeSlash())?.forEach {
        val file = ASSETS_DIR + it
        toInternalStorage(open(file), file, false)
    }

    list(PAGES_DIR.removeSlash())?.forEach {
        val file = PAGES_DIR + it
        toInternalStorage(open(file), file, false)
    }

    list(IMAGE_DIR.removeSlash())?.forEach {
        val file = IMAGE_DIR + it
        toInternalStorage(open(file), file, false)
    }

}

/**
 * Toggles a RecyclerView and an "empty state" placeholder view based on item count.
 * Receiver is the list size: 0 hides the RecyclerView and shows [searchView] (the empty
 * state placeholder), non-zero does the reverse.  Despite the parameter name [searchView]
 * it is used for any empty-state view, not necessarily a search widget.
 */
fun Int.noItemFound(recyclerview: View, searchView: View) {
    if (this == 0) {
        searchView.visibility = View.VISIBLE
        recyclerview.visibility = View.GONE
    } else {
        searchView.visibility = View.GONE
        recyclerview.visibility = View.VISIBLE
    }
}


fun Context.html2text(file: String): String {
    return Jsoup.parse(assets.open(file), null, "").text()
}


fun View.toggleVisibility(boolean: Boolean) {
    visibility = if (boolean) View.VISIBLE else View.GONE
}


//fun Activity.getBottomNavigationView(): BottomNavigationView =
//    findViewById(R.id.bottomNavigationView)
/////
fun Activity.getBottomNavigationView(): BottomNavigationView? {
    return findViewById(R.id.bottomNavigationView)
}

fun BottomNavigationView.isChecked(@IdRes id: Int) {
    menu.findItem(id).isChecked = true
}


fun Context.alertDialog(
    title: String = "Attention",
    message: String,
    positiveButtonCallback: () -> Unit,
): AlertDialog = AlertDialog.Builder(this)
    .setTitle(title)
    .setMessage(message)
    .setNegativeButton("no", null)
    .setPositiveButton("yes") { _, _ ->
        positiveButtonCallback()
    }.show()