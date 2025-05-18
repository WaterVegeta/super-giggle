package com.example.tabsgpttutor.shcedule

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.R
import com.google.android.material.button.MaterialButton
import androidx.core.graphics.toColorInt
import com.google.android.material.color.MaterialColors
import com.google.android.material.theme.overlay.MaterialThemeOverlay

class CalendarAdapter(
    private val context: Context,
    private var dataList: ArrayList<DataClass>,
    private val onItemLongClick: (DataClass, Int) -> Unit,
    private val onDone: (DataClass, Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

     var animatedPositions: MutableList<Int> = mutableListOf()
    private var lastAnimatedIndex: Int? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lessonText: TextView = itemView.findViewById(R.id.lessonText)
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val homeworkText: TextView = itemView.findViewById(R.id.homeworkText)
//        val homeIdText: TextView = itemView.findViewById(R.id.homeIdTV)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val btnDone: MaterialButton = itemView.findViewById(R.id.isItDoneBtn)

        init {
            itemView.setOnLongClickListener {
                animateSelection(cardView)
                itemView.postDelayed({
                    onItemLongClick(dataList[adapterPosition], adapterPosition)
                }, 100)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = dataList[position]
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
            if (item.done == true) {
                holder.btnDone.apply {
                    setTextColor(blendColors(secondaryCont, onSecondaryCont, 0.7f))
                    setIconTint(ColorStateList.valueOf(blendColors(secondaryCont, onSecondaryCont, 0.7f)))
                    setIconResource(R.drawable.baseline_done_24)
                    text = "виконано"
                    isEnabled = false

                }
                holder.homeworkText.setTextColor(blendColors(secondaryCont, onSecondaryCont, 0.7f))
//                holder.btnDone.setTextColor("#049805".toColorInt())
            }
            else{
                holder.btnDone.apply {
                    setTextColor(onTertiary)
                    setIconTint(ColorStateList.valueOf(onTertiary))
                    setIconResource(R.drawable.outline_close_24)
                    text = "не виконано"
                    isEnabled = true
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
                if (dataList.size > 1){
                    holder.cardView.setBackgroundResource(R.drawable.ripple_top)
                }else{
                    holder.cardView.setBackgroundResource(R.drawable.all_corners)
                }

            }
            dataList.size-1 -> holder.cardView.setBackgroundResource(R.drawable.ripple_bottom)
            else -> holder.cardView.setBackgroundResource(R.drawable.ripple_default)

        }

        holder.btnDone.setOnClickListener {
            onDone(item, position)
        }

    }

    override fun getItemCount() = dataList.size

    fun updateItem(updatedItem: DataClass, itemPosition: Int) {
        dataList[itemPosition] = updatedItem
        notifyItemChanged(itemPosition)
        Log.d("RVnewItem", "updatedItem: ${updatedItem}, updatedID: ${updatedItem.id}, index: $itemPosition")

    }
    private fun animateSelection(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100) // Slower animation
            .withEndAction {
                view.animate()
                    .setInterpolator(DecelerateInterpolator())
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(140) // Slower back
                    .start()
            }
            .start()
    }
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
}