package com.example.tabsgpttutor.schedule_change.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.TimeChange
import com.example.tabsgpttutor.schedule_change.adapters.TimeChangeAdapter.ViewHolder

class ChooseTimeAdapter(val addTime: (TimeChange) -> Unit): ListAdapter<TimeChange, ViewHolder>(
    TimeChangeAdapter.TimeDiffUtill()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return TimeChangeAdapter.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_lesson_change, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.rvDelete.visibility = View.GONE

        val currentItem = getItem(position)
        val startMinute = when(currentItem.lessonStartMinute){
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
            else -> currentItem.lessonStartMinute
        }

        val endMinute = when(currentItem.lessonEndMinute){
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
            else -> currentItem.lessonEndMinute
        }


        val timeText = currentItem.lessonStartHour + ":" + startMinute + "–" +currentItem.lessonEndHour + ":" + endMinute
        holder.rvLesson.text = timeText

        holder.rvCardView.setOnClickListener {
            addTime(currentItem)
        }
    }
}