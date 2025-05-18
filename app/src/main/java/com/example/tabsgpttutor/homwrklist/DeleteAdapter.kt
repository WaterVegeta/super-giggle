package com.example.tabsgpttutor.homwrklist

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.Homework
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.homwrklist.RecyclerAdapter.RecyclerViewHolder
import com.example.tabsgpttutor.shcedule.CalendarAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DeleteAdapter(private var dataList: List<Homework>): RecyclerView.Adapter<DeleteAdapter.ViewHolder>() {

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
        val currentItem = dataList[position]
        val date = LocalDate.parse(currentItem.date, formatterInput)
        val formattedDate = date.format(formatterOutput)
        holder.rvLesson.text = currentItem.lesson
        holder.rvTitle.text = currentItem.note
        holder.rvDueDate.text = formattedDate
        val surfacecContLow = MaterialColors.getColor(holder.btnDone, R.attr.colorSurfaceContainerLow)
        val onSurface = MaterialColors.getColor(holder.btnDone, R.attr.colorOnSurface)
        val onTertiary = MaterialColors.getColor(holder.btnDone, R.attr.colorTertiary)
        if (currentItem.done == true) {
            holder.btnDone.apply {
                setTextColor(blendColors(surfacecContLow, onSurface, 0.7f))
                setIconTint(ColorStateList.valueOf(blendColors(surfacecContLow, onSurface, 0.7f)))
                setIconResource(R.drawable.baseline_done_24)
                text = "виконано"
                isEnabled = false

            }
            holder.rvTitle.setTextColor(blendColors(surfacecContLow, onSurface, 0.7f))
//                holder.btnDone.setTextColor("#049805".toColorInt())
        }
        else {
            holder.btnDone.apply {
                setTextColor(onSurface)
                setIconTint(ColorStateList.valueOf(onSurface))
                setIconResource(R.drawable.outline_close_24)
                text = "не виконано"
                isEnabled = true
            }
            holder.rvTitle.setTextColor(onSurface)
        }
    }

    override fun getItemCount(): Int = dataList.size

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLesson: TextView = itemView.findViewById(R.id.lessonView)
        val rvTitle: TextView = itemView.findViewById(R.id.titleView)
        val rvDueDate: TextView = itemView.findViewById(R.id.dueDateView)
        val btnDone: MaterialButton = itemView.findViewById(R.id.isItDoneBtn)
    }
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    fun updateData(newList: List<Homework>){
        dataList = newList
        notifyDataSetChanged()
    }
}