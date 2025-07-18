package com.example.tabsgpttutor.settings.schedule_change.fragments

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.SettingsViewModel
import com.example.tabsgpttutor.data_base.Homework
import com.example.tabsgpttutor.data_base.shedule.LessonAndTime
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.data_base.temp_schedule.TempLessonAndTime
import com.example.tabsgpttutor.data_base.TimeChange

class DeleteDbFragment : PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, "delete_db")

        findPreference<Preference>("delete_lesson_list")?.setOnPreferenceClickListener {
            viewModel.delete(LessonChange())
            true
        }

        findPreference<Preference>("delete_time_list")?.setOnPreferenceClickListener {
            viewModel.delete(TimeChange())
            true
        }
        findPreference<Preference>("delete_schedule")?.setOnPreferenceClickListener {
            viewModel.delete(LessonAndTime())
            true
        }

        findPreference<Preference>("delete_homework")?.setOnPreferenceClickListener {
            viewModel.delete(Homework())
            true
        }
        findPreference<Preference>("delete_temp_schedule")?.setOnPreferenceClickListener {
            viewModel.delete(TempLessonAndTime())
            true
        }

    }
}