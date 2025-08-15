package com.example.task_king.widget

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.core.content.edit

object WidgetPreferences {
    private const val ERROR = -1

    private const val PREFS_NAME = "WidgetPrefs"
    private const val SYSTEM = 0
    private const val DAY = 1
    private const val NIGHT = 2
    private const val DYNAMIC = 3
    private const val CUSTOM = 69
    private const val defaultAlpha = 0.8f
    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAlpha(
        context: Context,
        appWidgetId: Int,
        alpha: Float) {
        Log.i("Widget save id", "id : $appWidgetId")
        getPrefs(context).edit() {
//            putInt("color_$appWidgetId", color)
//            putInt("text_color_$appWidgetId", textColor)
//            putInt("mode_$appWidgetId", mode)
            putFloat("alpha_$appWidgetId", alpha)
        }
    }

    fun getAlpha(context: Context?, appWidgetId: Int): Float{
        if (context != null){
        return getPrefs(context).getFloat("alpha_$appWidgetId", defaultAlpha)
        }else return defaultAlpha
    }

//    fun getMode(context: Context?, appWidgetId: Int): Int{
//        if (context != null){
//        return getPrefs(context).getInt("mode_$appWidgetId", ERROR)
//        }else return ERROR
//    }
//
//    fun loadColor(context: Context?, appWidgetId: Int): Int {
//        if (context != null){
//            return getPrefs(context)
//                .getInt("color_$appWidgetId", Color.TRANSPARENT)
//        }else return Color.TRANSPARENT
//    }
//
//    fun loadTextColor(context: Context?, appWidgetId: Int): Int {
//        if (context != null){
//            return getPrefs(context)
//                .getInt("text_color_$appWidgetId", Color.WHITE)
//        }else return Color.WHITE
//    }

    fun deletePrefs(context: Context, appWidgetId: Int) {
        getPrefs(context).edit() {
            remove("alpha_$appWidgetId")
        }
    }
}