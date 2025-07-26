package com.example.task_king.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.task_king.MyDynamic
import com.example.task_king.R
import com.example.task_king.data_base.shedule.Schedule
import io.realm.kotlin.ext.query
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScheduleWidgetProvider: AppWidgetProvider() {
    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?) {

        if (appWidgetIds != null) {
            for (id in appWidgetIds) {

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
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)) // required for uniqueness

                val views = RemoteViews(context?.packageName, R.layout.shedule_widget)
                views.setRemoteAdapter(R.id.listView, intent)
                views.setEmptyView(R.id.listView, android.R.id.empty)

                val weekDate = when(date.plusDays(k.toLong()).dayOfWeek){
                    DayOfWeek.MONDAY -> context?.resources?.getString(R.string.monday)
                    DayOfWeek.TUESDAY -> context?.resources?.getString(R.string.tuesday)
                    DayOfWeek.WEDNESDAY -> context?.resources?.getString(R.string.wednesday)
                    DayOfWeek.THURSDAY -> context?.resources?.getString(R.string.thursday)
                    DayOfWeek.FRIDAY -> context?.resources?.getString(R.string.friday)
                    DayOfWeek.SATURDAY -> context?.resources?.getString(R.string.saturday)
                    DayOfWeek.SUNDAY -> context?.resources?.getString(R.string.sunday)
                }

                val formatter = DateTimeFormatter.ofPattern("dd MMMM")
                val formate = date.plusDays(k.toLong()).format(formatter)

                views.setTextViewText(R.id.dateText, formate.toString())
                views.setTextViewText(R.id.weekText, weekDate)

                appWidgetManager?.updateAppWidget(id, views)
            }
        }

//        if (appWidgetIds != null) {
//            for (id in appWidgetIds) {
//                val views = RemoteViews(context?.packageName, R.layout.shedule_widget)
//                views.setTextViewText(R.id.widget_text, "Updated!")
//
//                appWidgetManager?.updateAppWidget(id, views)
//            }
//        }

    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }



}