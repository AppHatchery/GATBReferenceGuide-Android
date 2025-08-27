package org.apphatchery.gatbreferenceguide.ui

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.ActivityMainBinding
import org.apphatchery.gatbreferenceguide.prefs.UserPrefs
import org.apphatchery.gatbreferenceguide.utils.*
import javax.inject.Inject
import android.widget.EditText
import android.view.inputmethod.EditorInfo


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ActionBarController {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var preferenceManager: SharedPreferences
    
    // Action bar components
    private lateinit var actionBarTitle: TextView
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarSpacer: View

    // Search view properties - will be initialized when needed
    private var searchViewContainer: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupActionBar()
        setupNavigation()
        setupRemoteConfig()
        
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        searchState.exitSearchMode()
    }
    
    private fun setupActionBar() {
        supportActionBar?.elevation = 0f
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
            actionBar.setCustomView(R.layout.custom_action_bar_title)
            
            // Get references to action bar components
            val customView = actionBar.customView
            actionBarTitle = customView.findViewById(R.id.action_bar_title)
            actionBarBackButton = customView.findViewById(R.id.action_bar_back_button)
            actionBarSpacer = customView.findViewById(R.id.action_bar_spacer)

            // Set up back button click listener
            actionBarBackButton.setOnClickListener {
                onSupportNavigateUp()
            }
        }
    }
    
    private fun setupNavigation() {
        navController = findNavController(R.id.nav_host_fragment_container)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.itemTextColor = ContextCompat.getColorStateList(this, android.R.color.black)
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Handle bottom navigation visibility
            when (destination.id) {
                R.id.chapterFragment, R.id.subChapterFragment, R.id.bodyFragment, 
                R.id.body_web_view, R.id.chartFragment -> bottomNavigationView.visibility = View.GONE
                else -> bottomNavigationView.visibility = View.VISIBLE
            }
            
            // icon switching
            updateBottomNavIcons(destination.id)
            
            // Update action bar based on destination
            updateActionBar(destination.id, destination.label?.toString())
        }

        binding.bottomNavigationView.setupWithNavController(navController)
        // Removed this line to the prevent default back arrow:
        // setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
    }
    
    private fun setupRemoteConfig() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }
    
    private fun updateActionBar(destinationId: Int, destinationLabel: String?) {
        when (destinationId) {
            R.id.mainFragment -> {
                setActionBarConfig(
                    title = getString(R.string.guide),
                    showBackButton = false
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.savedFragment -> {
                setActionBarConfig(
                    title = "My Bookmarks",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.chapterFragment -> {
                setActionBarConfig(
                    title = "All Chapters",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.subChapterFragment -> {
                setActionBarConfig(
                    title = destinationLabel ?: "Chapters",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.bodyFragment -> {
                setActionBarConfig(
                    title = "", // Will be set by BodyFragment
                    showBackButton = true
                )
            }
            R.id.chartFragment -> {
                setActionBarConfig(
                    title = "All Charts", 
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.globalSearchFragment -> {
                setActionBarConfig(
                    title = "Search",
                    showBackButton = false
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.settingsFragment -> {
                setActionBarConfig(
                    title = "Settings",
                    showBackButton = false
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.contactFragment -> {
                setActionBarConfig(
                    title = "Contacts",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.contactAddFragment -> {
                setActionBarConfig(
                    title = "New Contact",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.privacyPolicy -> {
                setActionBarConfig(
                    title = "Privacy Policy",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.about -> {
                setActionBarConfig(
                    title = "About Us",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.contactDetailsFragment -> {
                setActionBarConfig(
                    title = "Public Contact",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            R.id.myContactDetailsFragment -> {
                setActionBarConfig(
                    title = "Private Contact",
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
            else -> {
                setActionBarConfig(
                    title = destinationLabel ?: getString(R.string.guide),
                    showBackButton = true
                )
                setActionBarSearchVisible(false) // Contract toolbar
            }
        }
    }
    
    // Implement ActionBarController interface
    override fun setActionBarTitle(title: String) {
        actionBarTitle.text = title
    }
    
    override fun setActionBarConfig(title: String, showBackButton: Boolean) {
        actionBarTitle.text = title
        
        if (showBackButton) {
            actionBarBackButton.visibility = View.VISIBLE
            actionBarSpacer.visibility = View.VISIBLE
        } else {
            actionBarBackButton.visibility = View.GONE
            actionBarSpacer.visibility = View.GONE
        }
    }

    // Implement the new interface methods
    override fun setActionBarSearchVisible(visible: Boolean) {
        Log.d("MainActivity", "setActionBarSearchVisible called with: $visible")
        // Find the search view container in the current fragment
        val navHostFragment = findViewById<View>(R.id.nav_host_fragment_container)
        val searchView = navHostFragment?.findViewById<View>(R.id.search_view_include)
        
        searchView?.visibility = if (visible) View.VISIBLE else View.GONE
        
        Log.d("MainActivity", "Search view visibility: ${searchView?.visibility}")
    }
    


    override fun setupActionBarSearch(onSearchAction: (String) -> Unit, onSearchIconClick: (String) -> Unit) {
        val navHostFragment = findViewById<View>(R.id.nav_host_fragment_container)
        val searchView = navHostFragment?.findViewById<View>(R.id.search_view_include)
        val searchEditText = searchView?.findViewById<EditText>(R.id.search_edit_text)
        
        searchEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchQuery = searchEditText.text.toString().trim()
                if (searchQuery.isNotEmpty()) {
                    onSearchAction(searchQuery)
                }
                true
            } else {
                false
            }
        }
    }

    private fun updateBottomNavIcons(currentDestinationId: Int) {
        val menu = bottomNavigationView.menu

        // Reset all to inactive icons
        menu.findItem(R.id.mainFragment)?.setIcon(R.drawable.ic_baseline_home)
        menu.findItem(R.id.globalSearchFragment)?.setIcon(R.drawable.ic_baseline_search)
        menu.findItem(R.id.settingsFragment)?.setIcon(R.drawable.ic_baseline_settings_2)

        // Set active icon based on current destination
        when (currentDestinationId) {
            R.id.mainFragment -> {
                menu.findItem(R.id.mainFragment)?.setIcon(R.drawable.ic_baseline_home_color)
            }
            R.id.globalSearchFragment -> {
                menu.findItem(R.id.globalSearchFragment)?.setIcon(R.drawable.ic_baseline_search_color)
            }
            R.id.settingsFragment -> {
                menu.findItem(R.id.settingsFragment)?.setIcon(R.drawable.ic_baseline_settings_color)
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}