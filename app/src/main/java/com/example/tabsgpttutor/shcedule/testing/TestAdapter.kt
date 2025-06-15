package com.example.tabsgpttutor.shcedule.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.R
import com.google.android.material.button.MaterialButton

class TestAdapter(private val onItemLongClick: (TestData, Int) -> Unit): ListAdapter<TestData, TestAdapter.ViewHolder>(TestDiffUtill()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lessonText: TextView = itemView.findViewById(R.id.lessonText)
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val homeworkText: TextView = itemView.findViewById(R.id.homeworkText)
        //        val homeIdText: TextView = itemView.findViewById(R.id.homeIdTV)
        val cardView: CardView = itemView.findViewById(R.id.cardView)

        val btnDone: MaterialButton = itemView.findViewById(R.id.isItDoneBtn)
    }

    class TestDiffUtill: DiffUtil.ItemCallback<TestData>(){
        override fun areItemsTheSame(
            oldItem: TestData,
            newItem: TestData
        ): Boolean {
            return oldItem.objectId == newItem.objectId
        }

        override fun areContentsTheSame(
            oldItem: TestData,
            newItem: TestData
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder{
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val currentItem = getItem(position)
        holder.lessonText.text = currentItem.lessonName
        holder.homeworkText.apply {
            visibility = if (currentItem.homeworkNote.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            text = if (currentItem.homeworkNote.isNullOrEmpty()) null else currentItem.homeworkNote
        }
        holder.timeText.text = currentItem.lessonTime
        holder.cardView.setOnLongClickListener {
            onItemLongClick(currentItem, position)
            true
        }
    }
}