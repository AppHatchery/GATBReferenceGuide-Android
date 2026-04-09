// Fragment providing a font-size picker for the TB guide content (Small/Normal/Large/Larger).
// The user moves a slider to preview how quote text scales, and the selection is persisted to
// SharedPreferences under the key R.string.font_key as a 0–3 index string.
//
// Data flow: no ViewModel. Reads/writes via PreferenceManager.getDefaultSharedPreferences().
// The stored index (0=100%, 1=125%, 2=150%, 3=175%) is consumed by BodyFragment.updateFont()
// to set WebView.textZoom and scale inline SVG icons to match the chosen font size.
//
// Navigation: reached from SettingsFragment via actionSettingsFragmentToFontSizeFragment.
// Related: SettingsFragment, BodyFragment.updateFont(), R.string.font_key.
package org.apphatchery.gatbreferenceguide.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.FragmentFontSizeBinding

@AndroidEntryPoint
class FontSizeFragment : Fragment(R.layout.fragment_font_size) {

    private lateinit var bind: FragmentFontSizeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind = FragmentFontSizeBinding.bind(view)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setupFontSlider()
        updateFontPreview()
    }

    /**
     * Reads the current font index from SharedPreferences and scales the preview
     * quote text proportionally so the user sees a live sample before leaving the screen.
     */
    private fun updateFontPreview() {
        val fontIndex =
            sharedPreferences.getString(getString(R.string.font_key), "1")?.toInt() ?: 1
        val fontSizePercent = when (fontIndex) {
            0 -> 100 // Small
            1 -> 125 // Normal
            2 -> 150 // Large
            3 -> 175 // Larger
            else -> 125
        }

        // Apply font scaling to the quote text
        val baseQuoteSize = 16f
        val baseAuthorSize = 14f
        val scaleFactor = fontSizePercent / 125.0f

        bind.quoteText.textSize = baseQuoteSize * scaleFactor
        bind.authorText.textSize = baseAuthorSize * scaleFactor
    }

    /**
     * Configures the font size slider: positions it at the currently saved index and
     * writes the newly chosen index to SharedPreferences on every change, then refreshes
     * the live preview via [updateFontPreview].
     */
    private fun setupFontSlider() {
        val fontSettingsSlider: Slider = bind.fontSizeSlider
        val currentFontSizeIndex =
            sharedPreferences.getString(getString(R.string.font_key), "1")?.toInt() ?: 1

        // Set initial slider position
        when (currentFontSizeIndex) {
            0 -> fontSettingsSlider.value = 100F
            1 -> fontSettingsSlider.value = 125F
            2 -> fontSettingsSlider.value = 150F
            else -> fontSettingsSlider.value = 175F
        }

        // Listen for slider changes
        fontSettingsSlider.addOnChangeListener { _, value, _ ->
            val selectedIndex = when (value) {
                100F -> 0
                125F -> 1
                150F -> 2
                175F -> 3
                else -> 1
            }

            sharedPreferences.edit()
                .putString(getString(R.string.font_key), selectedIndex.toString())
                .apply()
            
            // Update preview immediately
            updateFontPreview()
        }
    }
}
