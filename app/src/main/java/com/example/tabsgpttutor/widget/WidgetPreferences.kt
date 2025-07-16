package com.example.tabsgpttutor.widget

import android.content.Context
import android.graphics.Color
import androidx.core.content.edit

object WidgetPreferences {
    private const val PREFS_NAME = "WidgetPrefs"
    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveColor(context: Context, appWidgetId: Int, color: Int, textColor: Int) {
        getPrefs(context).edit() {
            putInt("color_$appWidgetId", color)
            putInt("text_color_$appWidgetId", textColor)
        }
    }

    fun loadColor(context: Context?, appWidgetId: Int): Int {
        if (context != null){
            return getPrefs(context)
                .getInt("color_$appWidgetId", 0)
        }else return 0
    }

    fun loadTextColor(context: Context?, appWidgetId: Int): Int {
        if (context != null){
            return getPrefs(context)
                .getInt("text_color_$appWidgetId", Color.BLACK)
        }else return Color.BLACK
    }

    fun deletePrefs(context: Context, appWidgetId: Int) {
        getPrefs(context).edit() {
            remove("color_$appWidgetId")
            remove("text_color_$appWidgetId")
        }
    }
}