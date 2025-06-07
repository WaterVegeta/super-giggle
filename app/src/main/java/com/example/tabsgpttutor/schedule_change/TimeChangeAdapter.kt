package com.example.tabsgpttutor.schedule_change

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.TimeChange

class TimeChangeAdapter(
    private val onEdit: (TimeChange) -> Unit,
    private val onDelete: (TimeChange) -> Unit) : ListAdapter<TimeChange, TimeChangeAdapter.ViewHolder>(TimeDiffUtill()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_lesson_change, parent, false))
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
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
            onEdit(currentItem)
        }
        holder.rvDelete.setOnClickListener {
            onDelete(currentItem)
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLesson: TextView = itemView.findViewById(R.id.tvLesson)
        val rvCardView: CardView = itemView.findViewById(R.id.cardView)
        val rvDelete: ImageButton = itemView.findViewById(R.id.deleteBtn)
//        val rvEdit: ImageButton = itemView.findViewById(R.id.editBtn)
    }

    class TimeDiffUtill: DiffUtil.ItemCallback<TimeChange>(){
        override fun areItemsTheSame(
            oldItem: TimeChange,
            newItem: TimeChange
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TimeChange,
            newItem: TimeChange
        ): Boolean {
            return oldItem.lessonStartHour == newItem.lessonStartHour
                    && oldItem.lessonStartMinute == newItem.lessonStartMinute
        }

    }
}