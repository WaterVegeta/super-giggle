package com.example.tabsgpttutor.schedule_change

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.schedule_change.LessonChangeAdapter.LessonDiffUtill
import com.example.tabsgpttutor.schedule_change.LessonChangeAdapter.ViewHolder

class AddLessonScheduleAdapter(val addLesson: (LessonChange) -> Unit): ListAdapter<LessonChange, LessonChangeAdapter.ViewHolder>(LessonDiffUtill()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LessonChangeAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_lesson_change, parent, false))
    }

    override fun onBindViewHolder(
        holder: LessonChangeAdapter.ViewHolder,
        position: Int
    ) {
        val list = getItem(position)
//        holder.rvEdit.visibility = View.GONE
        holder.rvDelete.visibility = View.GONE
        holder.rvLesson.text = list.lesson

        holder.rvCardView.setOnClickListener {
            addLesson(list)
        }
    }


}