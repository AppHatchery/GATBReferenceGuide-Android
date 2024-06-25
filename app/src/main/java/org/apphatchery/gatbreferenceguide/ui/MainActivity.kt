package org.apphatchery.gatbreferenceguide.ui

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.ActivityMainBinding
import org.apphatchery.gatbreferenceguide.ui.fragments.BodyFragment
import org.apphatchery.gatbreferenceguide.ui.viewmodels.MainActivityViewModel
import org.apphatchery.gatbreferenceguide.utils.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var preferenceManager: SharedPreferences
    private val viewModel : MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.elevation = 0f
        //setSupportActionBar(findViewById(R.id.my_toolbar))
        //supportActionBar?.setDisplayShowTitleEnabled(false);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        navController = findNavController(R.id.nav_host_fragment_container)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chapterFragment, R.id.subChapterFragment, R.id.bodyFragment, R.id.body_web_view, R.id.chartFragment -> bottomNavigationView.visibility =
                    View.GONE

                else -> bottomNavigationView.visibility = View.VISIBLE
            }
        }

        binding.bottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        searchState.exitSearchMode()

//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            supportActionBar?.title = destination.label
//            binding.toolbarTitle.text = destination.label
//            when(destination.id){
//                R.id.mainFragment -> binding.bookmark.visibility = View.VISIBLE
//               // R.id.bodyFragment -> binding.toolbarTitle.text =
//                else -> binding.bookmark.visibility  = View.GONE
//            }
//
//        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}