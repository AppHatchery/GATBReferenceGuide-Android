package org.apphatchery.gatbreferenceguide.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASettingsViewModel
import org.apphatchery.gatbreferenceguide.utils.dialog
import org.apphatchery.gatbreferenceguide.utils.safeDialogShow
import org.apphatchery.gatbreferenceguide.utils.toast

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel by viewModels<FASettingsViewModel>()


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        /*Privacy Policy OnPreferenceClickListener*/
        findPreference<Preference>(getString(R.string.privacy_policy_key)).also {
            it?.setOnPreferenceClickListener {
                findNavController().navigate(SettingsFragmentDirections
                        .actionSettingsFragmentToPrivacyPolicy())
                true
            }
        }

        /*Reset App OnPreferenceClickListener*/
        findPreference<Preference>(getString(R.string.reset_key)).also {
            it?.setOnPreferenceClickListener {
                with(Dialog(requireContext()).dialog()) {
                    setContentView(R.layout.generic_dialog)
                    val message = findViewById<TextView>(R.id.message)
                    val yesButton = findViewById<View>(R.id.yesButton)
                    val noButton = findViewById<View>(R.id.noButton)
                    "Are you sure you want to reset all data ?".also { message.text = it }
                    noButton.setOnClickListener { dismiss() }
                    yesButton.setOnClickListener {
                        dismiss()
                        viewModel.resetInfo()
                        requireContext().toast("App data has been reset.")
                    }
                    safeDialogShow()
                }
                true
            }
        }


    }
}