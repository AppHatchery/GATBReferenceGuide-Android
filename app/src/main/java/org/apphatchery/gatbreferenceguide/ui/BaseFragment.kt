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

abstract class BaseFragment(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {
    abstract override fun onViewCreated(view: View, savedInstanceState: Bundle?)

    fun voiceSearchListener(resultLauncher: ActivityResultLauncher<Intent>) =
        resultLauncher.launch(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
                it.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            })


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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_global_search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


}