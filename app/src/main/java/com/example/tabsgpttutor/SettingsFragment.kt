package com.example.tabsgpttutor

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceFragment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat


class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }

    override fun onResume() {
        super.onResume()
        // Update the displayed value when returning to settings
        findPreference<ListPreference>("theme")?.value = ThemeHelper.getCurrentTheme()
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