package com.example.tabsgpttutor.homwrklist

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.util.Util
import com.example.tabsgpttutor.data_base.Homework
import com.example.tabsgpttutor.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.radiobutton.MaterialRadioButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DeleteAdapter
    : ListAdapter<Homework, DeleteAdapter.ViewHolder>(DeleteDifUtil()) {

    val formatterInput = DateTimeFormatter.ISO_LOCAL_DATE
    val formatterOutput = DateTimeFormatter.ofPattern("dd MM yyyy")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        val date = LocalDate.parse(currentItem.date, formatterInput)
        val formattedDate = date.format(formatterOutput)
        Log.i("currentData", " lesson: ${currentItem.lesson} note: ${currentItem.note}")

        holder.rvLesson.text = currentItem.lesson
        holder.rvTitle.text = currentItem.note
        holder.rvDueDate.text = formattedDate
        val surfacecContLow = MaterialColors.getColor(holder.rvTitle, R.attr.colorSurfaceContainerLow)
        val onSurface = MaterialColors.getColor(holder.rvTitle, R.attr.colorOnSurface)
        holder.radioBtn.isEnabled = false
        if (currentItem.done == true) {
            holder.radioBtn.isChecked = true
            holder.rvTitle.setTextColor(blendColors(surfacecContLow, onSurface, 0.7f))
            holder.rvTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
//                holder.btnDone.setTextColor("#049805".toColorInt())
        }
        else{
            holder.radioBtn.isChecked = false
            holder.rvTitle.setTextColor(onSurface)
            holder.rvTitle.paintFlags = 0
        }
        val imageAdapter = ImageAdapter(currentItem.images.toList(), addImage = {
            Log.d("wd", "dawdwad")
        },
            startFullScreen = {dwa, awd ->
                Log.d("dwad", "$dwa $awd")
            })
        holder.imageRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLesson: TextView = itemView.findViewById(R.id.lessonView)
        val rvTitle: TextView = itemView.findViewById(R.id.titleView)
        val rvDueDate: TextView = itemView.findViewById(R.id.dueDateView)
        val rvCardView: CardView = itemView.findViewById(R.id.cardView)
        //        val btnDone: MaterialButton = itemView.findViewById(R.id.isItDoneBtn)
        val radioBtn: MaterialRadioButton = itemView.findViewById(R.id.radioButton)
        val imageRv: RecyclerView = itemView.findViewById(R.id.imageRv)
    }
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

//    fun updateData(newList: List<Homework>){
//        dataList = newList
//        Log.i("newList","${dataList.map { it.lesson }}  ${dataList.map { it.note }}")
//        notifyDataSetChanged()
//    }

    class DeleteDifUtil : DiffUtil.ItemCallback<Homework>(){
        override fun areItemsTheSame(
            oldItem: Homework,
            newItem: Homework
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Homework,
            newItem: Homework
        ): Boolean {
            return oldItem.lesson == newItem.lesson
        }

    }
}