package com.example.task_king

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

object ThemeHelper {
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun applyTheme(themePreference: String, dynamicColorsEnabled: Boolean, context: Context) {
        // Apply night mode
        when (themePreference) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        // Apply dynamic colors if enabled and available
        if (dynamicColorsEnabled) {
            DynamicColors.applyToActivitiesIfAvailable(
                context.applicationContext as Application
            )
        }
    }

    fun getCurrentTheme(): String {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> THEME_LIGHT
            AppCompatDelegate.MODE_NIGHT_YES -> THEME_DARK
            else -> THEME_SYSTEM
        }
    }
}