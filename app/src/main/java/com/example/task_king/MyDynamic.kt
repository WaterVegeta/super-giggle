package com.example.task_king

import android.app.Application
import androidx.preference.PreferenceManager
import com.example.task_king.data_base.AnimationSettings
import com.example.task_king.data_base.Homework
import com.example.task_king.data_base.ImageItem
import com.example.task_king.data_base.shedule.LessonAndTime
import com.example.task_king.data_base.LessonChange
import com.example.task_king.data_base.shedule.Schedule
import com.example.task_king.data_base.temp_schedule.TempLessonAndTime
import com.example.task_king.data_base.temp_schedule.TempSchedule
import com.example.task_king.data_base.TimeChange
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.migration.AutomaticSchemaMigration


class MyDynamic: Application() {
    companion object{
        lateinit var realm: Realm

    }
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val themePreference = sharedPreferences.getString("theme", "system")
        val dynamicColorsEnabled = sharedPreferences.getBoolean("dynamic_colors", true)

        val appLanguage = sharedPreferences.getString("app_language", "default")

        ThemeHelper.applyTheme(themePreference.toString(), dynamicColorsEnabled, this)
        LanguageChanger.setLanguage(appLanguage.toString(), this)

//        DynamicColors.applyToActivitiesIfAvailable(this)
        val migration = AutomaticSchemaMigration { context ->
            val oldVersion = context.oldRealm.schemaVersion()
                // Realm automatically adds the 'done' field to schema
                // No need to do anything here!
        }
        val config = RealmConfiguration.Builder(
            schema = setOf(
                Homework::class, LessonChange::class,
                TimeChange::class, Schedule::class,
                LessonAndTime::class, TempSchedule::class,
                TempLessonAndTime::class, AnimationSettings::class,
                ImageItem::class)
        ).schemaVersion(19) // last one is 19
            .migration(migration)
            .compactOnLaunch()
            .build()
        realm = Realm.open(config)
    }
}

