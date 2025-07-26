package com.example.task_king.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.example.task_king.LanguageChanger
import com.example.task_king.R
import com.example.task_king.ThemeHelper
import com.example.task_king.settings.animation_pref.AnimationActivity
import com.example.task_king.settings.schedule_change.ChangeScheduleAct
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FragmentCreated", "SettingsFragment")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
        }
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

        val dynamicPreference = findPreference<SwitchPreferenceCompat>("dynamic_colors")
        if (Build.VERSION.SDK_INT >= 31){
            dynamicPreference?.setOnPreferenceChangeListener { _, newValue ->
                val themePreference = sharedPreferences.getString("theme", "system")
                ThemeHelper.applyTheme(themePreference.toString(), newValue as Boolean, requireContext())
                requireActivity().recreate()
                if (!newValue as Boolean){
                    Toast.makeText(requireContext(),
                        getString(R.string.pls_restart_the_app_to_apply_changes), Toast.LENGTH_LONG).show()
                }
                true
            }
        }
        else{
            dynamicPreference?.isVisible = false
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

        val languagePreference = findPreference<Preference>("language")
        val language = LanguageChanger.getLanguage()
        languagePreference?.summary = if(language == "default"){
            getString(R.string.set_to, getString(R.string.system_default))
        }else if (language == "uk"){
            "Українська"
        } else {
            "English"
        }


        languagePreference?.setOnPreferenceClickListener {
            showLanguageDiaolog()
            true
        }


    }

    fun showLanguageDiaolog(){
        val languages = arrayOf(getString(R.string.system_default),
            getString(R.string.english), getString(R.string.ukrainian))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.choose_language))
            .setItems(languages) { _, item ->
                when(item){
                    0 -> LanguageChanger.setLanguage("default", requireContext())
                    1 -> LanguageChanger.setLanguage("en", requireContext())
                    2 -> LanguageChanger.setLanguage("uk", requireContext())
                }
            }.setNegativeButton(getString(R.string.cancel), null)
                .show()
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

}