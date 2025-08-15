package com.example.task_king.settings.schedule_change.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.task_king.R
import com.example.task_king.data_base.LessonChange

class AddLessonScheduleAdapter(
    val addLesson: (LessonChange) -> Unit,
    val editLesson:(LessonChange) -> Unit,
    val deleteLesson:(LessonChange) -> Unit
    ): ListAdapter<LessonChange, AddLessonScheduleAdapter.ViewHolder>(LessonDiffUtill()) {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLesson: TextView = itemView.findViewById(R.id.tvLesson)
        val rvCardView: CardView = itemView.findViewById(R.id.cardView)
        val rvDelete: ImageButton = itemView.findViewById(R.id.deleteBtn)
//        val rvEdit: ImageButton = itemView.findViewById(R.id.editBtn)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_lesson_change, parent, false))
    }

    var editMode = false
    var deleteMode = false

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val list = getItem(position)
        holder.rvDelete.visibility = View.GONE
        holder.rvLesson.text = list.lesson

        if (editMode){
            holder.rvCardView.setOnClickListener {
                editLesson(list)
            }
        }
        else if (deleteMode){
            holder.rvCardView.setOnClickListener {
                deleteLesson(list)
            }
        }
        else{
            holder.rvCardView.setOnClickListener {
                addLesson(list)
            }
        }
    }
    class LessonDiffUtill: DiffUtil.ItemCallback<LessonChange>(){
        override fun areItemsTheSame(
            oldItem: LessonChange,
            newItem: LessonChange
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: LessonChange,
            newItem: LessonChange
        ): Boolean {
            return oldItem.lesson == newItem.lesson
        }

    }


}