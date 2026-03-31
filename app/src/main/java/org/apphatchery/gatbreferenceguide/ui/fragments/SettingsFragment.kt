package org.apphatchery.gatbreferenceguide.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
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
        const val CONTACT_EMAIL = "support@apphatchery.org"
    }

    private fun composeEmail() = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("mailto:?to=$CONTACT_EMAIL")
        startActivity(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val prefView = super.onCreateView(inflater, container, savedInstanceState)
        val wrapper = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        wrapper.addView(prefView)
        val footer = inflater.inflate(R.layout.settings_footer, wrapper, false)
        val density = resources.displayMetrics.density
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM
            bottomMargin = (25 * density).toInt()
        }
        footer.layoutParams = params
        wrapper.addView(footer)

        val versionName = try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
                .versionName
        } catch (e: Exception) { "" }
        val year = Calendar.getInstance().get(Calendar.YEAR)
        footer.findViewById<TextView>(R.id.version).text = "Version $versionName"
        footer.findViewById<TextView>(R.id.powered_by).text = "Powered by AppHatchery $year"

        return wrapper
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(null)
        setDividerHeight(0)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Font Size preference -> opens font settings page
        findPreference<Preference>(getString(R.string.font_key))?.setOnPreferenceClickListener {
            findNavController().navigate(
                SettingsFragmentDirections
                    .actionSettingsFragmentToFontSizeFragment()
            )
            true
        }

        // Dark Mode switch
        findPreference<SwitchPreference>("dark_mode_key")?.let {
            // Set initial state based on current theme
            val currentNightMode = AppCompatDelegate.getDefaultNightMode()
            it.isChecked = currentNightMode == AppCompatDelegate.MODE_NIGHT_YES
            
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val nightMode = if (newValue as Boolean) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                
                // Apply theme change with animation
                view?.postDelayed({
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                }, 200)
                
                true
            }
        }

        // Contact Us
        findPreference<Preference>(getString(R.string.contact_us_key))?.let {
            it.setOnPreferenceClickListener {
                composeEmail()
                true
            }
        }

        // Give Feedback
        findPreference<Preference>("give_feedback_key")?.let {
            it.setOnPreferenceClickListener {
                // Navigate to feedback or open email
                composeEmail()
                true
            }
        }

        // Legal (Privacy Policy)
        findPreference<Preference>(getString(R.string.privacy_policy_key)).also {
            it?.setOnPreferenceClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections
                        .actionSettingsFragmentToPrivacyPolicy()
                )
                true
            }
        }

        // About
        findPreference<Preference>(getString(R.string.about_us_key)).also {
            it?.setOnPreferenceClickListener {
                findNavController().navigate(
                    SettingsFragmentDirections
                        .actionSettingsFragmentToAbout()
                )
                true
            }
        }

        // Clear App Content (Reset)
        findPreference<Preference>(getString(R.string.reset_key)).also {
            it?.setOnPreferenceClickListener {
                with(Dialog(requireContext()).dialog()) {
                    setContentView(R.layout.generic_dialog)
                    val message = findViewById<TextView>(R.id.message)
                    val yesButton = findViewById<View>(R.id.yesButton)
                    val noButton = findViewById<View>(R.id.noButton)
                    "Are you sure you want to clear all app content?".also { message.text = it }
                    noButton.setOnClickListener { dismiss() }
                    yesButton.setOnClickListener {
                        dismiss()
                        viewModel.resetInfo(requireContext())
                        requireContext().toast("App content has been cleared.")
                    }
                    safeDialogShow()
                }
                true
            }
        }
    }
}