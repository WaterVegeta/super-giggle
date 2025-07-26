package com.example.task_king.widget

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.task_king.MyDynamic
import com.example.task_king.R
import com.example.task_king.data_base.shedule.Schedule
import com.google.android.material.color.MaterialColors
import io.realm.kotlin.ext.query
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class DynamicWidProvider: AppWidgetProvider() {
    val SYSTEM = 0
    val DAY = 1
    val NIGHT = 2
    val DYNAMIC = 3
    val CUSTOM = 69

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray) {

        if (true) {
            for (id in appWidgetIds) {
                updateWidget(context, appWidgetManager, id)
//                val date = LocalDate.now()
//                val time = LocalTime.now()
//
//                val realm = MyDynamic.realm
//                val items = realm.query<Schedule>().find()
//                var k = 0
//                for (i in items){
//                    if (i.dayOfWeek == date.dayOfWeek.toString()){
//                        i.lessonAndTime.findLast { it.lessonEndHour.isNotEmpty() && it.lessonEndMinute.isNotEmpty() }?.let {
//                            val endMin = it.lessonEndMinute
//                            val endHour = it.lessonEndHour
//                            k = if (time.hour.toInt() >= endHour.toInt() && time.minute.toInt() >= endMin.toInt()){
//                                1
//                            } else 0
//                        }
//                        break
//
//                    }
//                }
//
//                val intent = Intent(context, ScheduleWidgetService::class.java)
//                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
//                intent.data =
//                    Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)) // required for uniqueness
//
//                val rv = RemoteViews(context?.packageName, R.layout.wid_dynamic)
//                rv.setRemoteAdapter(R.id.listView, intent)
//                rv.setEmptyView(R.id.listView, android.R.id.empty)
//
//                val weekDate = when (date.plusDays(k.toLong()).dayOfWeek) {
//                    DayOfWeek.MONDAY -> context?.resources?.getString(R.string.monday)
//                    DayOfWeek.TUESDAY -> context?.resources?.getString(R.string.tuesday)
//                    DayOfWeek.WEDNESDAY -> context?.resources?.getString(R.string.wednesday)
//                    DayOfWeek.THURSDAY -> context?.resources?.getString(R.string.thursday)
//                    DayOfWeek.FRIDAY -> context?.resources?.getString(R.string.friday)
//                    DayOfWeek.SATURDAY -> context?.resources?.getString(R.string.saturday)
//                    DayOfWeek.SUNDAY -> context?.resources?.getString(R.string.sunday)
//                }
//
//                val formatter = DateTimeFormatter.ofPattern("dd MMMM ")
//                val formate = date.plusDays(k.toLong()).format(formatter)
//
//                rv.setTextViewText(R.id.dateText, formate.toString())
//                rv.setTextViewText(R.id.weekText, weekDate)
//
//                appWidgetManager?.updateAppWidget(id, rv)
            }
//            super.onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager?, appWidgetId: Int) {
//            var colorPref = WidgetPreferences.loadColor(context, appWidgetId)
            var colorPref = getMaterialYouColor(context)
            val textColorPref = WidgetPreferences.loadTextColor(context, appWidgetId)
            val surfaceMode = WidgetPreferences.getMode(context, appWidgetId)

//            colorPref = resolveDynamicColor(context, R.attr.colorSecondaryContainer)
//            if (context != null) colorPref = MaterialColors.getColor(context, R.attr.colorSecondaryContainer, Color.GREEN)
//            val lsit = FloatArray(3)
//            Color.colorToHSV(colorPref, lsit)
//            lsit[2] = lsit[2] -0.07f
//            colorPref = Color.HSVToColor(lsit)

            val date = LocalDate.now()
            val time = LocalTime.now()

            val realm = MyDynamic.realm
            val items = realm.query<Schedule>().find()
            var k = 0
            for (i in items){
                if (i.dayOfWeek == date.dayOfWeek.toString()){
                    i.lessonAndTime.findLast { it.lessonEndHour.isNotEmpty() && it.lessonEndMinute.isNotEmpty() }?.let {
                        val endMin = it.lessonEndMinute
                        val endHour = it.lessonEndHour
                        k = if (time.hour.toInt() >= endHour.toInt() && time.minute.toInt() >= endMin.toInt()){
                            1
                        } else 0
                    }
                    break

                }
            }

            val intent = Intent(context, ScheduleWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data =
                Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)) // required for uniqueness

            val rv = RemoteViews(context?.packageName, R.layout.wid_dynamic)
            rv.setRemoteAdapter(R.id.listView, intent)
            rv.setEmptyView(R.id.listView, android.R.id.empty)

            val weekDate = when (date.plusDays(k.toLong()).dayOfWeek) {
                DayOfWeek.MONDAY -> context?.resources?.getString(R.string.monday)
                DayOfWeek.TUESDAY -> context?.resources?.getString(R.string.tuesday)
                DayOfWeek.WEDNESDAY -> context?.resources?.getString(R.string.wednesday)
                DayOfWeek.THURSDAY -> context?.resources?.getString(R.string.thursday)
                DayOfWeek.FRIDAY -> context?.resources?.getString(R.string.friday)
                DayOfWeek.SATURDAY -> context?.resources?.getString(R.string.saturday)
                DayOfWeek.SUNDAY -> context?.resources?.getString(R.string.sunday)
            }

            val formatter = DateTimeFormatter.ofPattern("dd MMMM ")
            val formate = date.plusDays(k.toLong()).format(formatter)

            rv.setTextViewText(R.id.dateText, formate.toString())
            rv.setTextViewText(R.id.weekText, weekDate)

//            rv.setTextColor(R.id.dateText, textColorPref)
//            rv.setTextColor(R.id.weekText, textColorPref)

            appWidgetManager?.updateAppWidget(appWidgetId, rv)

//            rv.setInt(R.id.dynamicWid, "setBackgroundColor", colorPref)

            appWidgetManager?.updateAppWidget(appWidgetId, rv)
        }

        private fun getMaterialYouColor(context: Context): Int {
            return MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, Color.RED)
//            val wallpaperManager = WallpaperManager.getInstance(context)
//            val wallpaperColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
//            return wallpaperColors?.primaryColor?.toArgb() ?: Color.GRAY
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (appWidgetIds != null) {
            if (context != null){
                for (appWidgetId in appWidgetIds) {
                    WidgetPreferences.deletePrefs(context, appWidgetId)
                }

            }
        }
        super.onDeleted(context, appWidgetIds)
    }

}