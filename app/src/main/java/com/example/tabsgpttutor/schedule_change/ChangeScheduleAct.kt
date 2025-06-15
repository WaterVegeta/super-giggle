package com.example.tabsgpttutor.schedule_change

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.SettingsViewModel
import com.example.tabsgpttutor.schedule_change.fragments.AddScheduleFragment
import com.example.tabsgpttutor.schedule_change.fragments.ChangeScheduleFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChangeScheduleAct : AppCompatActivity() {
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        appBar.post { appBar.setExpanded(false, false) }

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        updateTitle("Configure schedule")

        loadFragment(ChangeScheduleFragment())

    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("onSupportNavigateUp", "onSupportNavigateUp pressed")
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Log.d("onBackPressed", "onBackPressed")
        val currentFragment = supportFragmentManager.findFragmentById(R.id.settings_container)
        if (currentFragment is ChangeScheduleFragment){
            super.onBackPressed()
        }
        else if (currentFragment is AddScheduleFragment && IsDataChanged.getChanged()){
            MaterialAlertDialogBuilder(this)
                .setTitle("Save the changes")
                .setPositiveButton("Save") { dialog, _ ->
                    Log.d("saved", "saved")
                    viewModel.saveTempToSchedule()
                    loadFragment(ChangeScheduleFragment())
                }
                .setNegativeButton("Cancel"){ dialog, _ ->
                    Log.d("not saved", "not saved")
                    viewModel.copyScheduleToTemp()
                    loadFragment(ChangeScheduleFragment())
                }
                .create()
                .show()
        }
        else{
            loadFragment(ChangeScheduleFragment())
            updateTitle("Configure schedule")
        }
    }

    fun updateTitle(title: String){
        runOnUiThread {
            // Update both toolbar and collapsing toolbar
//            supportActionBar?.title = title
            collapsingToolbar.title = title
        }
    }

    fun loadFragment(frag: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, frag)
            .commit()
    }


//    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return true
//    }


}