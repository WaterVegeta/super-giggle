package com.example.tabsgpttutor

import android.app.Activity
import android.app.Application
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.utilities.DynamicColor
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.migration.RealmMigration


class MyDynamic: Application() {
    companion object{
        lateinit var realm: Realm

    }
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme", "system")
        val dynamicColorsEnabled = sharedPreferences.getBoolean("dynamic_colors", true)

        ThemeHelper.applyTheme(themePreference.toString(), dynamicColorsEnabled, this)

//        DynamicColors.applyToActivitiesIfAvailable(this)
        val migration = AutomaticSchemaMigration { context ->
            val oldVersion = context.oldRealm.schemaVersion()
            if (oldVersion < 2L) {
                // Realm automatically adds the 'done' field to schema
                // No need to do anything here!
            }
        }
        val config = RealmConfiguration.Builder(
            schema = setOf(Homework::class)
        ).schemaVersion(2) // 🔁 Bump this each time the schema changes
            .migration(migration)
            .compactOnLaunch()
            .build()
        realm = Realm.open(config)
    }
}

