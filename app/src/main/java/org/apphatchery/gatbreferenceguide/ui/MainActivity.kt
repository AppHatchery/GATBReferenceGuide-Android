package org.apphatchery.gatbreferenceguide.ui

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import org.apphatchery.gatbreferenceguide.R
import org.apphatchery.gatbreferenceguide.databinding.ActivityMainBinding
import org.apphatchery.gatbreferenceguide.ui.viewmodels.MainActivityViewModel
import org.apphatchery.gatbreferenceguide.utils.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var preferenceManager: SharedPreferences
    //viewmodel
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)


        setContentView(binding.root)
        supportActionBar?.elevation = 0f
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        navController = findNavController(R.id.nav_host_fragment_container)


        binding.bottomNavigationView.setupWithNavController(navController)
        binding.chapterBottomNavigationView.setupWithNavController(navController)
       setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        searchState.exitSearchMode()

        viewModel.title.observe(this) { newTitle ->
            supportActionBar?.title = newTitle
            binding.toolbarTitle.text = newTitle
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label
            binding.toolbarTitle.text = destination.label
            when(destination.id){
                R.id.mainFragment -> binding.bookmark.visibility = View.VISIBLE
                else -> binding.bookmark.visibility  = View.GONE
            }

        }


        binding.chapterBottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.share -> {
                    TODO()
                }
                R.id.bookmark -> {
                    TODO()
                }
                R.id.note -> {
                    TODO()
                }
                else -> {
                    navController.navigate(menuItem.itemId)
                    true
                }
            }
        }

        binding.bookmark.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_savedFragment)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}