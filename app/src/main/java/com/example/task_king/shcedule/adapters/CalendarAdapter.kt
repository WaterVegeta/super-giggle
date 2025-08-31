package com.example.task_king.shcedule.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.task_king.R
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.task_king.TouchAnimation
import com.example.task_king.shcedule.DataClass
import com.google.android.material.color.MaterialColors

class CalendarAdapter(
    private val onItemLongClick: (DataClass, Int) -> Unit,
    private val onDone: (DataClass, Int) -> Unit,
    val context: Context
) : ListAdapter<DataClass, CalendarAdapter.ViewHolder>(ScheduleDiffUtill()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val layout = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
//            R.layout.item_lesson
//        }else{ R.layout.item_landscape}
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lessonText: TextView = itemView.findViewById(R.id.lessonText)
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val homeworkText: TextView = itemView.findViewById(R.id.homeworkText)
        //        val homeIdText: TextView = itemView.findViewById(R.id.homeIdTV)
        val cardView: CardView = itemView.findViewById(R.id.cardView)

        val btnDone: MaterialButton = itemView.findViewById(R.id.isItDoneBtn)
        init {
            itemView.setOnLongClickListener {
//                animateSelection(cardView)
//                onItemLongClick(getItem(position), position)
                TouchAnimation.release(cardView, 140)
                itemView.postDelayed({
                    onItemLongClick(getItem(position), position)
                }, 100)
                true
            }
//            itemView.setOnClickListener {
//                animateSelection(cardView)
//                true
//            }
            itemView.setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_DOWN -> TouchAnimation
                        .touch(cardView, 100, 0.9f, 0.9f)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> TouchAnimation
                        .release(cardView, 140)
                }
                false
            }
        }

    }

    class ScheduleDiffUtill : DiffUtil.ItemCallback<DataClass>() {
        override fun areItemsTheSame(oldItem: DataClass, newItem: DataClass): Boolean {
            // Use hwId if it's more stable than UUID
            return oldItem.hwId == newItem.hwId
        }
        override fun areContentsTheSame(oldItem: DataClass, newItem: DataClass): Boolean {
            return oldItem.contentEquals(newItem)
        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = getItem(position)
        holder.lessonText.text = item.subject
        holder.timeText.text = item.time

        if (!item.homework.isNullOrEmpty()) {
            val secondaryCont = MaterialColors.getColor(holder.btnDone, R.attr.colorSecondaryContainer)
            val onSecondaryCont = MaterialColors.getColor(holder.btnDone, R.attr.colorOnSecondaryContainer)
            val onTertiary = MaterialColors.getColor(holder.btnDone, R.attr.colorTertiary)
            holder.homeworkText.apply {
                visibility = View.VISIBLE
                text = item.homework

            }
            holder.btnDone.visibility = View.VISIBLE
            holder.btnDone.setOnClickListener {
                onDone(item, position)
            }
            if (item.done == true) {
                holder.btnDone.apply {
                    setTextColor(blendColors(secondaryCont, onSecondaryCont, 0.7f))
                    setIconTint(ColorStateList.valueOf(blendColors(secondaryCont, onSecondaryCont, 0.7f)))
                    setIconResource(R.drawable.baseline_done_24)
                    text = context.getString(R.string.done_hw)
                    isEnabled = false
                    isClickable = false

                }
                holder.homeworkText.setTextColor(blendColors(secondaryCont, onSecondaryCont, 0.7f))
//                holder.btnDone.setTextColor("#049805".toColorInt())
            }
            else{
                holder.btnDone.apply {
                    setTextColor(onTertiary)
                    setIconTint(ColorStateList.valueOf(onTertiary))
                    setIconResource(R.drawable.outline_close_24)
                    text = context.getString(R.string.not_done_hw)
                    isEnabled = true
                    isClickable = true
                }
                holder.homeworkText.setTextColor(onSecondaryCont)
            }
//            holder.homeIdText.text = item.id
        } else {
            holder.homeworkText.visibility = View.INVISIBLE
            holder.btnDone.visibility = View.GONE
        }

        when(position){
            0 ->{
                if (currentList.size > 1){
                    holder.cardView.setBackgroundResource(R.drawable.top_corners)
                }else{
                    holder.cardView.setBackgroundResource(R.drawable.all_corners)
                }

            }
            currentList.size-1 -> holder.cardView.setBackgroundResource(R.drawable.bottom_corners)
            else -> holder.cardView.setBackgroundResource(R.drawable.no_corners)

        }



    }


    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
}
