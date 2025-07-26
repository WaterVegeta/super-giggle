package com.example.task_king.settings.schedule_change.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.task_king.R
import com.example.task_king.data_base.temp_schedule.TempLessonAndTime
import com.example.task_king.data_base.temp_schedule.TempSchedule
import java.time.DayOfWeek

class SchedulePagerAdapter(
    private val addLesson: (TempSchedule) -> Unit,
    val addTime: (TempLessonAndTime, TempSchedule)-> Unit,
    val deleteItem: (TempLessonAndTime, TempSchedule) -> Unit,
    val changeLesson: (TempLessonAndTime, Boolean, TempSchedule) -> Unit
) : ListAdapter<TempSchedule, SchedulePagerAdapter.DayViewHolder>(ScheduleDiffCallback()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parent_schedule, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.tvDayOfWeek.text = getDayOfWeek(holder.itemView.context, schedule.dayOfWeek)
//        holder.lessonsRecyclerView.itemAnimator = DefaultItemAnimator().apply {
//            addDuration = 9999
//            changeDuration = 250
//            moveDuration = 250
//            removeDuration = 250
//        }

        holder.lessonsAdapter.submitList(schedule.lessonAndTime.toList())

        holder.btnAddLesson.setOnClickListener {
            addLesson(schedule)
        }
    }

    fun getDayOfWeek(context: Context, dayOfWeek: String): String{
        return when(dayOfWeek){
            DayOfWeek.MONDAY.name.uppercase() -> context.getString(R.string.monday)
            DayOfWeek.TUESDAY.name.uppercase() -> context.getString(R.string.tuesday)
            DayOfWeek.WEDNESDAY.name.uppercase() -> context.getString(R.string.wednesday)
            DayOfWeek.THURSDAY.name.uppercase() -> context.getString(R.string.thursday)
            DayOfWeek.FRIDAY.name.uppercase() -> context.getString(R.string.friday)
            DayOfWeek.SATURDAY.name.uppercase() -> context.getString(R.string.saturday)
            DayOfWeek.SUNDAY.name.uppercase() -> context.getString(R.string.sunday)
            else -> { "ERROR"}
        }
    }


    class ScheduleDiffCallback : DiffUtil.ItemCallback<TempSchedule>() {
        override fun areItemsTheSame(oldItem: TempSchedule, newItem: TempSchedule): Boolean {
            return oldItem.id == newItem.id // Compare by unique ID
        }

        override fun areContentsTheSame(oldItem: TempSchedule, newItem: TempSchedule): Boolean {
            return oldItem.dayOfWeek == newItem.dayOfWeek &&
                    oldItem.changeFuctor == newItem.changeFuctor

        }
    }
    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        val lessonsRecyclerView: RecyclerView = itemView.findViewById(R.id.childRv)
        val btnAddLesson: Button = itemView.findViewById(R.id.btnAddLesson)
        val lessonsAdapter = ChildScheduleAdapter(
            { item -> addTime(item, getItem(position)) },
            deleteItem = { item -> deleteItem(item, getItem(position)) },
            changeLesson = { item, isEven -> changeLesson(item, isEven, getItem(position)) }
        )
        init {
            lessonsRecyclerView.itemAnimator = DefaultItemAnimator().apply {
                addDuration = 500
                changeDuration = 500
                moveDuration = 300
                removeDuration = 500
            }
            lessonsRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = lessonsAdapter
                setHasFixedSize(true)
            }
        }
    }

}