package com.example.tabsgpttutor.homwrklist

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.Homework
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.shcedule.DataClass
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.math.sqrt

class RecyclerAdapter(private var dataList: List<Homework>,
                      private val listener: OnItemClickListener,
                      private val onDone: (Homework) -> Unit):
    RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>() {
        private val selectedItems = mutableSetOf<Int>()

    interface OnItemClickListener {
        fun onItemLongClick(position: Int)
        fun onItemClick(position: Int)
    }
    fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
    }

    fun getSelectedItems(): List<Int> = selectedItems.toList()



    fun clearSelection() {
        val oldSelection = selectedItems.toList()
        selectedItems.clear()
        oldSelection.forEach { notifyItemChanged(it) }
    }

    private val animatedPositions = mutableSetOf<Int>()
    val formatterInput = DateTimeFormatter.ISO_LOCAL_DATE
    val formatterOutput = DateTimeFormatter.ofPattern("dd MMM")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int
    ) {
        val currentItem = dataList[position]
        val currentDate = LocalDate.now()
        val date = LocalDate.parse(currentItem.date, formatterInput)
        val formattedDate = date.format(formatterOutput)
        val context = holder.itemView.context

        if (currentDate.toString() == currentItem.date){
            holder.rvDueDate.text = "Сьогодні"
        }
        else if (currentDate.plusDays(1).toString() == currentItem.date){
            holder.rvDueDate.text = "Завтра"
        }
        else if (currentDate.plusDays(2).toString() == currentItem.date){
            val dayOfWeek = currentDate.plusDays(2).dayOfWeek
            holder.rvDueDate.text = when(dayOfWeek){
                DayOfWeek.MONDAY -> context.resources.getString(R.string.monday)
                DayOfWeek.TUESDAY -> context.resources.getString(R.string.tuesday)
                DayOfWeek.WEDNESDAY -> context.resources.getString(R.string.wednesday)
                DayOfWeek.THURSDAY -> context.resources.getString(R.string.thursday)
                DayOfWeek.FRIDAY -> context.resources.getString(R.string.friday)
                DayOfWeek.SATURDAY -> context.resources.getString(R.string.saturday)
                DayOfWeek.SUNDAY -> context.resources.getString(R.string.sunday)
            }
        }
        else if (currentDate.plusDays(3).toString() == currentItem.date){
            val dayOfWeek = currentDate.plusDays(3).dayOfWeek
            holder.rvDueDate.text = when(dayOfWeek){
                DayOfWeek.MONDAY -> context.resources.getString(R.string.monday)
                DayOfWeek.TUESDAY -> context.resources.getString(R.string.tuesday)
                DayOfWeek.WEDNESDAY -> context.resources.getString(R.string.wednesday)
                DayOfWeek.THURSDAY -> context.resources.getString(R.string.thursday)
                DayOfWeek.FRIDAY -> context.resources.getString(R.string.friday)
                DayOfWeek.SATURDAY -> context.resources.getString(R.string.saturday)
                DayOfWeek.SUNDAY -> context.resources.getString(R.string.sunday)
            }
        }
        else if (currentDate.plusDays(4).toString() == currentItem.date){
            val dayOfWeek = currentDate.plusDays(4).dayOfWeek
            holder.rvDueDate.text = when(dayOfWeek){
                DayOfWeek.MONDAY -> context.resources.getString(R.string.monday)
                DayOfWeek.TUESDAY -> context.resources.getString(R.string.tuesday)
                DayOfWeek.WEDNESDAY -> context.resources.getString(R.string.wednesday)
                DayOfWeek.THURSDAY -> context.resources.getString(R.string.thursday)
                DayOfWeek.FRIDAY -> context.resources.getString(R.string.friday)
                DayOfWeek.SATURDAY -> context.resources.getString(R.string.saturday)
                DayOfWeek.SUNDAY -> context.resources.getString(R.string.sunday)
            }
        }
        else{
            holder.rvDueDate.text = formattedDate
        }

        holder.rvLesson.text = currentItem.lesson
        holder.rvTitle.text = currentItem.note
        holder.rvCardView.setBackgroundResource(
            if (selectedItems.contains(position)) R.drawable.selected_hw_card else R.drawable.default_hw_card
        )

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }

        holder.itemView.setOnLongClickListener {
            listener.onItemLongClick(position)
            true
        }
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
        else{
            holder.btnDone.apply {
                setTextColor(onSurface)
                setIconTint(ColorStateList.valueOf(onSurface))
                setIconResource(R.drawable.outline_close_24)
                text = "не виконано"
                isEnabled = true
            }
            holder.rvTitle.setTextColor(onSurface)
        }
        holder.btnDone.setOnClickListener {
            onDone(currentItem)
        }
        val base = 1
        holder.rvCardView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_pop_in_rv).apply {
            startOffset = base.toLong()
        })


    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class RecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLesson: TextView = itemView.findViewById(R.id.lessonView)
        val rvTitle: TextView = itemView.findViewById(R.id.titleView)
        val rvDueDate: TextView = itemView.findViewById(R.id.dueDateView)
        val rvCardView: CardView = itemView.findViewById(R.id.cardView)
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