// Abstract base class for all TB guide fragments. Defines the shared lifecycle contract:
// every subclass must implement onViewCreated(). Provides helpers that forward action bar
// control requests to MainActivity (which implements ActionBarController), keeping fragments
// decoupled from the Activity's concrete toolbar implementation.
//
// Shared capabilities:
//   - setActionBarTitle/setActionBarConfig: update the custom toolbar title and back-button state
//   - voiceSearchListener / voiceSearchForActivityResult: launch and handle Android voice
//     recognition (used by GlobalSearchFragment for hands-free search input)
//
// All screen-specific fragments (ChapterFragment, BodyFragment, etc.) extend BaseFragment.
// Related: ActionBarController, MainActivity, OnToolbarBackPressed.
package org.apphatchery.gatbreferenceguide.ui

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import org.apphatchery.gatbreferenceguide.R
import android.view.inputmethod.EditorInfo
import android.widget.EditText

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    abstract override fun onViewCreated(view: View, savedInstanceState: Bundle?)

    // Action bar control methods
    protected fun setActionBarTitle(title: String) {
        (requireActivity() as? ActionBarController)?.setActionBarTitle(title)
    }

    protected fun setActionBarConfig(title: String, showBackButton: Boolean = true) {
        (requireActivity() as? ActionBarController)?.setActionBarConfig(title, showBackButton)
    }

    protected fun setActionBarSearchVisible(visible: Boolean) {
        (requireActivity() as? ActionBarController)?.setActionBarSearchVisible(visible)
    }

    protected fun setupActionBarSearch(onSearchAction: (String) -> Unit, onSearchIconClick: (String) -> Unit) {
        (requireActivity() as? ActionBarController)?.setupActionBarSearch(onSearchAction, onSearchIconClick)
    }

    /**
     * Launches the Android speech recognition Intent via [resultLauncher].
     * Used by GlobalSearchFragment to populate the search field via voice input.
     */
    fun voiceSearchListener(resultLauncher: ActivityResultLauncher<Intent>) =
        resultLauncher.launch(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
                it.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            })


    /**
     * Extracts the top-ranked speech recognition result from [activityResult] and passes
     * it to [activityResultCallback] so the calling fragment can populate its search field.
     */
    fun voiceSearchForActivityResult(
        activityResult: ActivityResult,
        activityResultCallback: (String) -> Unit
    ) {
        val matches = activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (matches != null && matches.size > 0) {
            val searchWrd = matches[0]
            if (!TextUtils.isEmpty(searchWrd)) {
                activityResultCallback(searchWrd)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_global_search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}