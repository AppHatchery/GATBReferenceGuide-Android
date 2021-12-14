package org.apphatchery.gatbreferenceguide.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.ui.viewmodels.FASettingsViewModel
import org.apphatchery.gatbreferenceguide.utils.dialog
import org.apphatchery.gatbreferenceguide.utils.safeDialogShow
import org.apphatchery.gatbreferenceguide.utils.toast
import java.util.*

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel by viewModels<FASettingsViewModel>()

    companion object {
        const val CONTACT_EMAIL = "morgan.greenleaf@emory.edu"
    }

    private fun composeEmail() = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("mailto:?to=$CONTACT_EMAIL")
        startActivity(this)
    }


    private fun setFontSummaryText(id: Int) = when (id) {
        0 -> "Small"
        2 -> "Large"
        3 -> "Larger"
        else -> "Normal"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<ListPreference>(getString(R.string.theme_key))?.let {
            it.summary =
                if (it.value.toString() == "system") "System default" else it.value.toString()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                when {
                    newValue.toString() == "light" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    newValue.toString() == "dark" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
                requireActivity().recreate()
                true
            }
        }

        findPreference<ListPreference>(getString(R.string.font_key))?.let {
            it.summary = setFontSummaryText(it.value.toString().toInt())
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                it.summary = setFontSummaryText(newValue.toString().toInt())
                true
            }

        }


        findPreference<Preference>(getString(R.string.contact_us_key))?.let {
            it.setOnPreferenceClickListener {
                composeEmail()
                true
            }
        }


        /*Privacy Policy OnPreferenceClickListener*/
        findPreference<Preference>(getString(R.string.privacy_policy_key)).also {
            it?.setOnPreferenceClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections
                        .actionSettingsFragmentToPrivacyPolicy()
                )
                true
            }
        }


        /*About Us OnPreferenceClickListener*/
        findPreference<Preference>(getString(R.string.about_us_key)).also {
            it?.setOnPreferenceClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections
                        .actionSettingsFragmentToAbout()
                )
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