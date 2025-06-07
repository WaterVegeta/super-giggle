package com.example.tabsgpttutor.schedule_change

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.tabsgpttutor.R

class ChangeScheduleFragment: PreferenceFragmentCompat(){
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, "change_schedule_screen")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        findPreference<Preference>("change_add_lesson")?.setOnPreferenceClickListener {
            Log.d("Preferences", "change_add_lesson was clicked")
            atachFragment(AddLessonFragment(), "Add/change lesson")
            true // Return true if the click is handled.
        }

        findPreference<Preference>("change_add_time")?.setOnPreferenceClickListener {
            Log.d("Preferences", "change_add_lesson was clicked")
            atachFragment(AddTimeFragment(), "Add/change time")
            true // Return true if the click is handled.
        }

        findPreference<Preference>("change_schedule")?.setOnPreferenceClickListener {
            Log.d("Preferences", "change_add_lesson was clicked")
            atachFragment(AddScheduleFragment(), "Change schedule")
            true // Return true if the click is handled.
        }
        findPreference<Preference>("pref_delete_db")?.setOnPreferenceClickListener {
            Log.d("Preferences", "pref_delete_db")
            atachFragment(DeleteDbFragment(), "Delete db")
            true // Return true if the click is handled.
        }
    }

    fun atachFragment(fragment: Fragment, title: String){
        (activity as ChangeScheduleAct).apply {
            loadFragment(fragment)
            updateTitle(title)
        }
    }


}