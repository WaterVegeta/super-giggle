package com.example.tabsgpttutor.settings.animation_pref

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.tabsgpttutor.R

class AnimationFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, "animation_screen")

        findPreference<Preference>("animation_homework")?.setOnPreferenceClickListener {
//            Log.d("Preferences", "pref_delete_db")
            atachFragment(AnimationScheduleFragment("Homework"), getString(R.string.homework_animation))
            true // Return true if the click is handled.
        }
        findPreference<Preference>("animation_schedule")?.setOnPreferenceClickListener {
//            Log.d("Preferences", "pref_delete_db")
            atachFragment(AnimationScheduleFragment("Schedule"), getString(R.string.schedule_animation))
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