package com.example.task_king.homwrklist

import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class CustomHwListAnim : DefaultItemAnimator(){
    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        if (addDuration == 120L){
            addDuration = 300
        }


        Log.d("addDuration", "addDuration: $addDuration")

        holder?.itemView?.apply {
            alpha = 0f
            scaleX = 0.7f
            scaleY = 0.7f
            pivotX = holder.itemView.width /2f
            pivotY = holder.itemView.height /2f
        }

        holder?.itemView?.animate()?.apply {
            scaleY(1f)
            scaleX(1f)
            alpha(1f)
            duration = addDuration
            interpolator = OvershootInterpolator()
            withEndAction { dispatchAddFinished(holder) }
            start()
        }

        return true

    }
}