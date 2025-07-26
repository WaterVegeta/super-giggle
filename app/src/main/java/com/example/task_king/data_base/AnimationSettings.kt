package com.example.task_king.data_base

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class AnimationSettings : RealmObject {
    @PrimaryKey var _id : String = UUID.randomUUID().toString()
    var whatView : String = ""

    var firstAnim : Boolean = false
    var firstInterpolator : String = "LinearInterpolator"

    var firstScaleX : Float = 1f
    var firstScaleY : Float = 1f

    var firstDuration : Long = 300

    var firstAlpha : Float = 1f

    var firstTranslationX: Float = 0f
    var firstTranslationY: Float = 0f

    var pivotX : Float = 0f
    var pivotY : Float = 0f

    var secondAnim: Boolean = false

    var secondInterpolator : String = "DecelerateInterpolator"

    var secondScaleX : Float = 1f
    var secondScaleY : Float = 1f

    var secondAlpha : Float = 1f

    var secondTranslationX : Float = 0f
    var secondTranslationY : Float = 0f

    var secondDuration : Long = 200
}
