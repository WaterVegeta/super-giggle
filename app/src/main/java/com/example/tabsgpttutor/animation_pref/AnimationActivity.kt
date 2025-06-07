package com.example.tabsgpttutor.animation_pref

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.schedule_change.AddScheduleFragment
import com.example.tabsgpttutor.schedule_change.ChangeScheduleFragment
import com.example.tabsgpttutor.schedule_change.IsDataChanged
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AnimationActivity : AppCompatActivity() {

    private lateinit var collapsingToolbar: CollapsingToolbarLayout
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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        updateTitle("Configure animation")

        loadFragment(AnimationFragment())
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("onSupportNavigateUp", "onSupportNavigateUp pressed")
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Log.d("onBackPressed", "onBackPressed")
        val currentFragment = supportFragmentManager.findFragmentById(R.id.settings_container)
        if (currentFragment is AnimationFragment){
            super.onBackPressed()
        }
        else if (currentFragment is AnimationHwFragment && IsDataChanged.getChanged() ||
            currentFragment is AnimationScheduleFragment && IsDataChanged.getChanged()){
            MaterialAlertDialogBuilder(this)
                .setTitle("Save the changes")
                .setPositiveButton("Save") { dialog, _ ->
                    Log.d("saved", "saved")
//                    viewModel.saveTempToSchedule()
                    if (currentFragment is AnimationScheduleFragment) currentFragment.saveData()
                    loadFragment(AnimationFragment())
                }
                .setNegativeButton("Cancel"){ dialog, _ ->
                    Log.d("not saved", "not saved")
                    loadFragment(AnimationFragment())
                }
                .create()
                .show()
            IsDataChanged.dataNotChanged()
        }
        else{
            loadFragment(AnimationFragment())
            updateTitle("Configure animation")
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
}