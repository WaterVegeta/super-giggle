package com.example.tabsgpttutor.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.ThemeHelper
import com.example.tabsgpttutor.animation_pref.AnimationActivity
import com.example.tabsgpttutor.schedule_change.ChangeScheduleAct
import java.util.prefs.Preferences

class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FragmentCreated", "SettingsFragment")

    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Theme change listener
        findPreference<ListPreference>("theme")?.setOnPreferenceChangeListener { _, newValue ->
            val dynamicColorsEnabled = sharedPreferences.getBoolean("dynamic_colors", true)
            ThemeHelper.applyTheme(newValue.toString(), dynamicColorsEnabled, requireContext())
            requireActivity().recreate()
            true
        }

        // Dynamic colors change listener
        findPreference<SwitchPreferenceCompat>("dynamic_colors")?.setOnPreferenceChangeListener { _, newValue ->
            val themePreference = sharedPreferences.getString("theme", "system")
            ThemeHelper.applyTheme(themePreference.toString(), newValue as Boolean, requireContext())
            requireActivity().recreate()
            true
        }

        findPreference<Preference>("change_act")?.setOnPreferenceClickListener {
            Log.d("Preferences", "ScheduleChange was clicked")
            val intent = Intent(requireContext(), ChangeScheduleAct::class.java)
            startActivity(intent)
            true // Return true if the click is handled.
        }
        findPreference<Preference>("animation")?.setOnPreferenceClickListener {
            Log.d("Preferences", "ScheduleChange was clicked")
            val intent = Intent(requireContext(), AnimationActivity::class.java)
            startActivity(intent)
            true // Return true if the click is handled.
        }

    }

    override fun onResume() {
        super.onResume()
        // Update the displayed value when returning to settings
        findPreference<ListPreference>("theme")?.value = ThemeHelper.getCurrentTheme()
        Log.d("FragmentResumed", "SettingsFragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FragmentDestroyed", "SettingsFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("FragmentDestroyedView", "SettingsFragment")
    }



//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_settings, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val items = listOf(
//            view.findViewById<TextView>(R.id.settingsText)
//        )
//
//        val baseDelay = 100L
//        val duration = 350L
//        val interpolator = OvershootInterpolator(1.1f) // Gentle pop
//
//        for ((index, item) in items.withIndex()) {
//            item.alpha = 0f
//            item.scaleX = 0.9f
//            item.scaleY = 0.9f
//
//            item.animate()
//                .alpha(1f)
//                .scaleX(1f)
//                .scaleY(1f)
//                .setStartDelay(baseDelay * index)
//                .setDuration(duration)
//                .setInterpolator(interpolator)
//                .start()
//        }
//    }


}