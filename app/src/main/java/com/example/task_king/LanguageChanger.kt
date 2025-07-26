package com.example.task_king

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import androidx.core.content.edit

object LanguageChanger {

    fun setLanguage(language: String, context: Context){
        saveLanguage(language, context)
        if (language == "default"){
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }else{
            val appLocale = LocaleListCompat.forLanguageTags(language)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun saveLanguage(language: String, context: Context){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit() { putString("app_language", language) }
    }
    fun getLanguage(): String{
        val currentLanguage = AppCompatDelegate.getApplicationLocales()
        if (currentLanguage.isEmpty){
            return "default"
        }
        else{
            return currentLanguage[0].toString()
        }
    }
}