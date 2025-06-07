package com.example.tabsgpttutor

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout

import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

import com.example.tabsgpttutor.homwrklist.HomewListFragment
import com.example.tabsgpttutor.shcedule.ScheduleFrag
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("is_first_run", true)

        if (isFirstRun) {
            viewModel.firstSetUp()
            // Set the flag to false for future runs
            with(sharedPref.edit()) {
                putBoolean("is_first_run", false)
                apply()
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Connect BottomNavigationView with NavController
        findViewById<BottomNavigationView>(R.id.navBar).setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    //supportFragmentManager.beginTransaction()
    //            .replace(R.id.fmLayout, ScheduleFrag())
    //            .commit()
    //        var currentFrag = R.id.nav_schedule
    //
    //        navBar = findViewById(R.id.navBar)
    //        navBar.setOnItemSelectedListener { item ->
    //            if (item.itemId == currentFrag)
    //                return@setOnItemSelectedListener true
    //            currentFrag = item.itemId
    //
    //            when (item.itemId) {
    //                R.id.nav_schedule -> {
    //                    loadFragment(ScheduleFrag())
    //                    true
    //                }
    //
    //                R.id.nav_hwList -> {
    //                    loadFragment(HomewListFragment())
    //                    true
    //                }
    //
    //                R.id.nav_settings -> {
    //                    loadFragment(SettingsFragment())
    //                    true
    //                }
    //
    //                else -> false
    //            }
    //        }
    //
    //    }
    //
    //    fun loadFragment(fragment: Fragment) {
    //        supportFragmentManager.beginTransaction()
    //            .replace(R.id.fmLayout, fragment)
    //            .commit()
    //    }
}

