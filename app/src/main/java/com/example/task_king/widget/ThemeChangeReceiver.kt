package com.example.task_king.widget

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import com.google.android.material.color.MaterialColors
import kotlin.jvm.java

class ThemeChangeReceiver : BroadcastReceiver() {

    private var lastPrimaryColor: Int = Color.TRANSPARENT
    private var lastIsDark: Boolean = false

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val currentColor = getMaterialYouColor(context)
            val isDark = isDarkMode(context)

            // Avoid redundant updates
                if (context != null){
                    updateAllWidgets(context)
                }

        }
    }

    private fun getMaterialYouColor(context: Context): Int {
        return MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, Color.RED)
    }

    private fun isDarkMode(context: Context?): Boolean {
        val uiMode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, DynamicWidProvider::class.java)
        )
        val intent = Intent(context, DynamicWidProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        }
        context.sendBroadcast(intent)
    }
}