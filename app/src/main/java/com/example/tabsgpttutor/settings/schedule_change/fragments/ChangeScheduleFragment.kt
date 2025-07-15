package com.example.tabsgpttutor.settings.schedule_change.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.settings.schedule_change.ChangeScheduleAct

class ChangeScheduleFragment: PreferenceFragmentCompat(){
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, "change_schedule_screen")
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        findPreference<Preference>("change_add_lesson")?.setOnPreferenceClickListener {
            Log.d("Preferences", "change_add_lesson was clicked")
            atachFragment(AddLessonFragment(), getString(R.string.add_lesson))
            true // Return true if the click is handled.
        }

        findPreference<Preference>("change_add_time")?.setOnPreferenceClickListener {
            Log.d("Preferences", "change_add_lesson was clicked")
            atachFragment(AddTimeFragment(), getString(R.string.add_time))
            true // Return true if the click is handled.
        }

        findPreference<Preference>("change_schedule")?.setOnPreferenceClickListener {
            Log.d("Preferences", "change_add_lesson was clicked")
            atachFragment(AddScheduleFragment(), getString(R.string.change_schedule))
            true // Return true if the click is handled.
        }
        findPreference<Preference>("pref_delete_db")?.setOnPreferenceClickListener {
            Log.d("Preferences", "pref_delete_db")
            atachFragment(DeleteDbFragment(), getString(R.string.delete_db))
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