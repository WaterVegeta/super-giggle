package com.example.tabsgpttutor.widget

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.tabsgpttutor.R
import com.google.android.material.color.DynamicColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.coroutineContext

class DynamicWidProvider: AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?) {

        if (appWidgetIds != null) {
            for (id in appWidgetIds) {
                var k = 0
                val date = LocalDate.now()
                val time = LocalTime.now()

                when (date.dayOfWeek.toString()) {
                    "MONDAY" -> if (time.hour >= 15 && time.minute >= 5) {
                        k++
                    } else {
                        k = 0
                    }

                    "TUESDAY" -> if (time.hour >= 16 && time.minute >= 0) {
                        k++
                    } else {
                        k = 0
                    }

                    "WEDNESDAY" -> if (time.hour >= 15 && time.minute >= 5) {
                        k++
                    } else {
                        k = 0
                    }

                    "THURSDAY" -> if (time.hour >= 16 && time.minute >= 0) {
                        k++
                    } else {
                        k = 0
                    }

                    "FRIDAY" -> if (time.hour >= 14 && time.minute >= 10) {
                        k++
                        k++
                        k++
                    } else {
                        k = 0
                    }

                    "SATURDAY" -> {
                        k++
                        k++
                    }

                    "SUNDAY" -> {
                        k++
                    }

                }
                val intent = Intent(context, ScheduleWidgetService::class.java)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                intent.data =
                    Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)) // required for uniqueness

                val views = RemoteViews(context?.packageName, R.layout.wid_dynamic)
                views.setRemoteAdapter(R.id.listView, intent)
                views.setEmptyView(R.id.listView, android.R.id.empty)

                val weekDate = when (date.plusDays(k.toLong()).dayOfWeek) {
                    DayOfWeek.MONDAY -> context?.resources?.getString(R.string.monday)
                    DayOfWeek.TUESDAY -> context?.resources?.getString(R.string.tuesday)
                    DayOfWeek.WEDNESDAY -> context?.resources?.getString(R.string.wednesday)
                    DayOfWeek.THURSDAY -> context?.resources?.getString(R.string.thursday)
                    DayOfWeek.FRIDAY -> context?.resources?.getString(R.string.friday)
                    DayOfWeek.SATURDAY -> context?.resources?.getString(R.string.saturday)
                    DayOfWeek.SUNDAY -> context?.resources?.getString(R.string.sunday)
                }

                val formatter = DateTimeFormatter.ofPattern("dd MM yyy")
                val formate = date.plusDays(k.toLong()).format(formatter)

                views.setTextViewText(R.id.dateText, formate.toString())
                views.setTextViewText(R.id.weekText, weekDate)

                appWidgetManager?.updateAppWidget(id, views)
            }
        }
    }
}