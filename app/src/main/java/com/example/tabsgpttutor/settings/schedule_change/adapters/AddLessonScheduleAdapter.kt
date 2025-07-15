package com.example.tabsgpttutor.settings.schedule_change.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.settings.schedule_change.adapters.LessonChangeAdapter.LessonDiffUtill
import com.example.tabsgpttutor.settings.schedule_change.adapters.LessonChangeAdapter.ViewHolder

class AddLessonScheduleAdapter(val addLesson: (LessonChange) -> Unit): ListAdapter<LessonChange, ViewHolder>(LessonDiffUtill()) {
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
        val list = getItem(position)
//        holder.rvEdit.visibility = View.GONE
        holder.rvDelete.visibility = View.GONE
        holder.rvLesson.text = list.lesson

        holder.rvCardView.setOnClickListener {
            addLesson(list)
        }
    }


}