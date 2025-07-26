package com.example.task_king

import android.view.View
import android.view.animation.DecelerateInterpolator

object TouchAnimation {

    fun touch(view: View, duration: Long, scX: Float, scY: Float){
        view.animate()
            .setInterpolator(DecelerateInterpolator())
            .scaleX(scX)
            .scaleY(scY)
            .setDuration(duration)
            .start()
    }
    fun release(view: View, duration: Long){
        view.animate()
            .setInterpolator(DecelerateInterpolator())
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .start()
    }
}