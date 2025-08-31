package com.example.task_king

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log

import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController

import com.example.task_king.homwrklist.HomewListFragment
import com.example.task_king.homwrklist.SaveBigViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.content.edit
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val viewModel: SettingsViewModel by viewModels()

    lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val isFirstRun = getPrefs().getBoolean("is_first_run", true)

        if (isFirstRun) {
            viewModel.firstSetUp()
            // Set the flag to false for future runs
            with(getPrefs().edit()) {
                putBoolean("is_first_run", false)
                apply()
            }
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.welcome_to_tasking))
                .setMessage(getString(R.string.go_to_the_settings_to_configure_schedule))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
        val navBar = findViewById<BottomNavigationView>(R.id.navBar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            
            insets
        }

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navBar.setupWithNavController(navController)

    }

    private fun getPrefs() =
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

