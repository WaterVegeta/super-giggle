package com.example.tabsgpttutor.settings.schedule_change.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.TouchAnimation
import com.example.tabsgpttutor.data_base.temp_schedule.TempLessonAndTime

class ChildScheduleAdapter(
    val addTime: (TempLessonAndTime) -> Unit,
    val deleteItem: (TempLessonAndTime) -> Unit,
    val changeLesson: (TempLessonAndTime, Boolean) -> Unit
    )
    : ListAdapter<TempLessonAndTime, ChildScheduleAdapter.ViewHolder>(ChildLessonDiffUtill()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_child_schedule, parent, false))
        Log.d("ChildAdapter", "created")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = getItem(position)
        Log.d("ChildAdapter", "list: $list")
        holder.rvLesson.text = list.lessonScheduleOnOdd
        if (list.lessonStart.isEmpty()){
            holder.rvTime.text = "+ time"
        }else{
            val endMinute = when(list.lessonEndMinute){
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
                else -> list.lessonEndMinute
            }
            holder.rvTime.text = list.lessonStart + "–" + list.lessonEndHour + ":" + endMinute
        }

        holder.rvLessonEven.text = if (list.lessonSchedeleOnEven.isNullOrEmpty()){
            "+ on even"
        }else{
            list.lessonSchedeleOnEven
        }

        Log.d("ChildAdapter", "created")

        holder.rvTime.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    TouchAnimation.touch(v, 100, 0.9f, 0.9f)
                }
                MotionEvent.ACTION_UP -> {
                    TouchAnimation.release(v, 100)
                    addTime(list)
                }
                MotionEvent.ACTION_CANCEL ->{
                    TouchAnimation.release(v, 100)
                }
            }
            true
        }
        holder.rvDelete.setOnClickListener {
            deleteItem(list)
        }
        holder.rvLesson.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    TouchAnimation.touch(v, 100, 0.9f, 0.9f)
                }
                MotionEvent.ACTION_UP -> {
                    TouchAnimation.release(v, 100)
                    changeLesson(list, false)
                }
                MotionEvent.ACTION_CANCEL ->{
                    TouchAnimation.release(v, 100)
                }
            }
            true
        }

        holder.rvLessonEven.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    TouchAnimation.touch(v, 100, 0.9f, 0.9f)
                }
                MotionEvent.ACTION_UP -> {
                    TouchAnimation.release(v, 100)
                    changeLesson(list, true)
                }
                MotionEvent.ACTION_CANCEL ->{
                    TouchAnimation.release(v, 100)
                }
            }
            true
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLesson : TextView = itemView.findViewById(R.id.rvLessonChild)
        val rvLessonEven : TextView = itemView.findViewById(R.id.rvLessonChildEven)
        val rvTime : TextView = itemView.findViewById(R.id.rvTime)

        val rvDelete: ImageButton = itemView.findViewById(R.id.deleteBtn)


    }

    class ChildLessonDiffUtill: DiffUtil.ItemCallback<TempLessonAndTime>() {
        override fun areItemsTheSame(
            oldItem: TempLessonAndTime,
            newItem: TempLessonAndTime
        ): Boolean {
            return oldItem.objectId == newItem.objectId
        }

        override fun areContentsTheSame(
            oldItem: TempLessonAndTime,
            newItem: TempLessonAndTime
        ): Boolean {
            return oldItem.lessonStart == newItem.lessonStart &&
                    oldItem.lessonScheduleOnOdd == newItem.lessonScheduleOnOdd &&
                    oldItem.lessonSchedeleOnEven == newItem.lessonSchedeleOnEven
        }

    }
}