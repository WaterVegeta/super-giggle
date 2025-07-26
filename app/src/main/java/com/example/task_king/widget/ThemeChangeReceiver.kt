package com.example.task_king.widget

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import kotlin.jvm.java

class ThemeChangeReceiver : BroadcastReceiver() {

    private var lastPrimaryColor: Int = Color.TRANSPARENT
    private var lastIsDark: Boolean = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            val currentColor = getMaterialYouColor(context)
            val isDark = isDarkMode(context)

            // Avoid redundant updates
            if (currentColor != lastPrimaryColor || isDark != lastIsDark) {
                lastPrimaryColor = currentColor
                lastIsDark = isDark

                if (context != null){
                    updateAllWidgets(context)
                }
            }
        }
    }

    private fun getMaterialYouColor(context: Context?): Int {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val wallpaperColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        return wallpaperColors?.primaryColor?.toArgb() ?: Color.GRAY
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