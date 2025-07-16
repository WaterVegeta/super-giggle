package com.example.tabsgpttutor.widget

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.shedule.Schedule
import io.realm.kotlin.ext.query
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil

class ScheduleWidgetFactory(
    private val context: Context,
    private val id: Int): RemoteViewsService.RemoteViewsFactory {

        private val data = arrayListOf<WidDataClass>()
    private lateinit var lessonList: Array<String>
    private lateinit var timeList: Array<String>
    private var textColor: Int = Color.WHITE

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        data.clear()

        Log.i("Widget Factory", "id: $id")
        textColor = WidgetPreferences.loadTextColor(context, id)


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
                    k = if (time.hour.toInt() == endHour.toInt()){
                        if (time.minute.toInt() >= endMin.toInt()){
                            1
                        }
                        else{
                            0
                        }
                    }
                    else if(time.hour.toInt() >= endHour.toInt()){
                        1
                    }
                    else 0
                }
                break

            }
        }

        val newDate = date.plusDays(k.toLong())

        val isEvenWeek = (ceil(newDate.dayOfYear / 7.0).toInt() % 2 == 0)

        val schedule = realm.query<Schedule>("dayOfWeek == $0", newDate.dayOfWeek.name).find().first()

        schedule.lessonAndTime.map { lesson ->
            val subject = if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty())
                lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd

            val endMinute = when(lesson.lessonEndMinute){
                "0"->"00"
                "1"->"01"
                "2"->"02"
                "3"->"03"
                "4"->"04"
                "5"->"05"
                "6"->"06"
                "7"->"07"
                "8"->"08"
                "9"->"09"
                else -> lesson.lessonEndMinute
            }
            val time = if (lesson.lessonStart.isNotEmpty() && lesson.lessonEndHour.isNotEmpty() &&
                lesson.lessonEndMinute.isNotEmpty())"${lesson.lessonStart} - ${lesson.lessonEndHour}:${endMinute}"
            else ""

            data.add(WidDataClass(subject, time))
        }

//        timeList = context.resources.getStringArray(R.array.six)
//
//        val dayOfWeek = date.plusDays(k.toLong()).dayOfWeek
////        lessonList = when (dayOfWeek) {
////            DayOfWeek.MONDAY -> if (week % 2 == 0.0) context.resources.getStringArray(R.array.monday1)
////            else context.resources.getStringArray(R.array.monday2)
////            DayOfWeek.TUESDAY -> context.resources.getStringArray(R.array.tues)
////            DayOfWeek.WEDNESDAY -> context.resources.getStringArray(R.array.wend)
////            DayOfWeek.THURSDAY -> context.resources.getStringArray(R.array.thurs)
////            DayOfWeek.FRIDAY -> context.resources.getStringArray(R.array.frid)
////            else -> arrayOf("не ма")
////        }
//        lessonList = arrayOf("Yed ")
//        for (i in lessonList.indices) {
//            val subject = lessonList[i]
//            val timeLes = timeList[i]
//
//            data.add(WidDataClass(subject, timeLes))
//        }

    }

    override fun onDestroy() {
        data.clear()
    }

    override fun getCount(): Int = data.size

    override fun getViewAt(position: Int): RemoteViews? {
        val views = RemoteViews(context.packageName, R.layout.wid_schedule_item)

        views.setTextViewText(R.id.lessonText, data[position].subject)
        views.setTextViewText(R.id.timeText, data[position].time)

        views.setTextColor(R.id.lessonText, textColor)
        views.setTextColor(R.id.timeText, textColor)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}