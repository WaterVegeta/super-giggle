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
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.data_base.TimeChange

class LessonChangeAdapter(private val onEdit: (LessonChange) -> Unit,
                          private val onDelete: (LessonChange) -> Unit) : ListAdapter<LessonChange, LessonChangeAdapter.ViewHolder>(LessonDiffUtill()) {
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
        holder.rvLesson.text = currentItem.lesson.toString()

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