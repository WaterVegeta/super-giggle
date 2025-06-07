package com.example.tabsgpttutor.homwrklist

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.data_base.Homework
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.TouchAnimation
import com.example.tabsgpttutor.shcedule.CalendarAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import io.realm.kotlin.internal.platform.canWrite
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HwListAdapter(
    private val listener: OnItemClickListener,
    private val onDone: (Homework) -> Unit
) : ListAdapter<Homework, HwListAdapter.ViewHolder>(HwDiffUtill()) {


    private val selectedItems = mutableSetOf<String>()
    private var lastPosition = -1

        interface OnItemClickListener {
            fun onItemLongClick(itemId: String)
            fun onItemClick(itemId: String)
        }


    fun toggleSelection(itemId: String) {
//        if (position >= currentList.size) return
//        Log.d("ListAdapter", "position toggled/untoggled: $position")
//        val item = getItem(position)
        Log.d("ListAdapter", "itemId: $itemId")
        if (selectedItems.contains(itemId)) {
            selectedItems.remove(itemId)
        } else {
            selectedItems.add(itemId)
        }
        notifyItemChanged(currentList.indexOfFirst { it.id == itemId })
    }

    fun getSelectedIds(): List<String> = selectedItems.toList()

    fun clearSelection() {
//        val oldSelection = selectedItems.toSet()
        selectedItems.clear()
        notifyDataSetChanged()

        // Find positions for each ID and notify changes
//        oldSelection.forEach { id ->
//            val index = currentList.indexOfFirst { it.id == id }
//            if (index != -1) notifyItemChanged(index)
//        }
    }

    val formatterInput = DateTimeFormatter.ISO_LOCAL_DATE
    val formatterOutput = DateTimeFormatter.ofPattern("dd MMM")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        val currentDate = LocalDate.now()
        val date = LocalDate.parse(currentItem.date, formatterInput)
        val formattedDate = date.format(formatterOutput)
        val dueDate = holder.rvDueDate
        val context = holder.itemView.context


        when{
            currentDate.toString() == currentItem.date -> dueDate.text = "Сьогодні"
            currentDate.plusDays(1).toString() == currentItem.date -> dueDate.text = "Завтра"
            currentDate.plusDays(2).toString() == currentItem.date -> dueDate.text = getDayOfWeek(currentDate.plusDays(2).dayOfWeek, context)
            currentDate.plusDays(3).toString() == currentItem.date -> dueDate.text = getDayOfWeek(currentDate.plusDays(3).dayOfWeek, context)
            currentDate.plusDays(4).toString() == currentItem.date -> dueDate.text = getDayOfWeek(currentDate.plusDays(4).dayOfWeek, context)
            else -> dueDate.text = formattedDate
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(currentItem.id)
            Log.d("ListAdapter", "item clicked at position: $position")
        }

        holder.itemView.setOnTouchListener { v, event ->
            if (selectedItems.contains(currentItem.id)) {
                v.scaleX = 1f
                v.scaleY = 1f
                return@setOnTouchListener false // Don’t animate
            }
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    if (!selectedItems.isEmpty()){
                        v.animate().cancel()
                        TouchAnimation.release(v, 140)

                    }
                    else{
                        TouchAnimation.touch(v, 100, 0.9f, 0.9f)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    TouchAnimation.release(v, 140)
                }
            }
            false
        }
        holder.itemView.setOnLongClickListener {
            listener.onItemLongClick(currentItem.id)
            Log.d("ListAdapter", "item long clicked at position: $position")
            true
        }


        holder.rvLesson.text = currentItem.lesson
        holder.rvTitle.text = currentItem.note

        holder.rvCardView.setBackgroundResource(
            if (selectedItems.contains(currentItem.id)) R.drawable.selected_hw_card else R.drawable.default_hw_card
        )
        val surfacecContLow = MaterialColors.getColor(holder.btnDone, R.attr.colorSurfaceContainerLow)
        val onSurface = MaterialColors.getColor(holder.btnDone, R.attr.colorOnSurface)
        if (currentItem.done == true) {
            holder.btnDone.apply {
                setTextColor(blendColors(surfacecContLow, onSurface, 0.7f))
                setIconTint(ColorStateList.valueOf(blendColors(surfacecContLow, onSurface, 0.7f)))

                setIconResource(R.drawable.baseline_done_24)
                text = "виконано"
                isEnabled = false
                isClickable = false

            }
            holder.rvTitle.setTextColor(blendColors(surfacecContLow, onSurface, 0.7f))
//                holder.btnDone.setTextColor("#049805".toColorInt())
        }
        else{
            holder.btnDone.apply {
                setTextColor(onSurface)
                setIconTint(ColorStateList.valueOf(onSurface))
                setIconResource(R.drawable.outline_close_24)
                setOnClickListener { onDone(currentItem) }
                text = "не виконано"
                isEnabled = true
                isClickable = true
            }
            holder.rvTitle.setTextColor(onSurface)
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvLesson: TextView = itemView.findViewById(R.id.lessonView)
        val rvTitle: TextView = itemView.findViewById(R.id.titleView)
        val rvDueDate: TextView = itemView.findViewById(R.id.dueDateView)
        val rvCardView: CardView = itemView.findViewById(R.id.cardView)
        val btnDone: MaterialButton = itemView.findViewById(R.id.isItDoneBtn)
    }

    fun getDayOfWeek(dayOfWeek: DayOfWeek, context: Context): String {
        return when(dayOfWeek){
            DayOfWeek.MONDAY -> context.resources.getString(R.string.monday)
            DayOfWeek.TUESDAY -> context.resources.getString(R.string.tuesday)
            DayOfWeek.WEDNESDAY -> context.resources.getString(R.string.wednesday)
            DayOfWeek.THURSDAY -> context.resources.getString(R.string.thursday)
            DayOfWeek.FRIDAY -> context.resources.getString(R.string.friday)
            DayOfWeek.SATURDAY -> context.resources.getString(R.string.saturday)
            DayOfWeek.SUNDAY -> context.resources.getString(R.string.sunday)
        }
    }



    class HwDiffUtill: DiffUtil.ItemCallback<Homework>(){
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
            return oldItem.date == newItem.date &&
                    oldItem.lesson == newItem.lesson &&
                    oldItem.note == newItem.note &&
                    oldItem.done == newItem.done
        }

    }

    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    fun updateData(newList: List<Homework>) {
        submitList(newList) // 🚀 Smart updates!
    }
}