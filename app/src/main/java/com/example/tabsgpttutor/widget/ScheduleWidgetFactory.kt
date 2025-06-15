package com.example.tabsgpttutor.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.tabsgpttutor.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil

class ScheduleWidgetFactory(private val context: Context): RemoteViewsService.RemoteViewsFactory {
    private val data = arrayListOf<WidDataClass>()
    private lateinit var lessonList: Array<String>
    private lateinit var timeList: Array<String>

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
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



        val week = ceil(date.plusDays(k.toLong()).dayOfYear / 7.0)

        timeList = context.resources.getStringArray(R.array.six)

        val dayOfWeek = date.plusDays(k.toLong()).dayOfWeek
        lessonList = when (dayOfWeek) {
            DayOfWeek.MONDAY -> if (week % 2 == 0.0) context.resources.getStringArray(R.array.monday1)
            else context.resources.getStringArray(R.array.monday2)
            DayOfWeek.TUESDAY -> context.resources.getStringArray(R.array.tues)
            DayOfWeek.WEDNESDAY -> context.resources.getStringArray(R.array.wend)
            DayOfWeek.THURSDAY -> context.resources.getStringArray(R.array.thurs)
            DayOfWeek.FRIDAY -> context.resources.getStringArray(R.array.frid)
            else -> arrayOf("не ма")
        }
        for (i in lessonList.indices) {
            val subject = lessonList[i]
            val timeLes = timeList[i]

            data.add(WidDataClass(subject, timeLes))
        }

    }

    override fun onDestroy() {
        data.clear()
    }

    override fun getCount(): Int = data.size

    override fun getViewAt(position: Int): RemoteViews? {
        val views = RemoteViews(context.packageName, R.layout.wid_schedule_item)
        views.setTextViewText(R.id.lessonText, data[position].subject)
        views.setTextViewText(R.id.timeText, data[position].time)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}