package com.example.tabsgpttutor.animation_pref

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.schedule_change.ChangeScheduleAct
import com.example.tabsgpttutor.schedule_change.DeleteDbFragment

class AnimationFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, "animation_screen")

        findPreference<Preference>("animation_homework")?.setOnPreferenceClickListener {
//            Log.d("Preferences", "pref_delete_db")
            atachFragment(AnimationScheduleFragment("Homework"), "Homework animation")
            true // Return true if the click is handled.
        }
        findPreference<Preference>("animation_schedule")?.setOnPreferenceClickListener {
//            Log.d("Preferences", "pref_delete_db")
            atachFragment(AnimationScheduleFragment("Schedule"), "Schedule animation")
            true // Return true if the click is handled.
        }
    }

    fun atachFragment(fragment: Fragment, title: String){
        (activity as AnimationActivity).apply {
            loadFragment(fragment)
            updateTitle(title)
        }
    }
}