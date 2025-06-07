package com.example.tabsgpttutor.animation_pref

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.example.tabsgpttutor.HwViewModel
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.AnimationSettings
import com.example.tabsgpttutor.schedule_change.IsDataChanged
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.slider.Slider

class AnimationScheduleFragment(val whatView: String) : Fragment(R.layout.fragment_animation_schedule) {

    private val viewModel : HwViewModel by viewModels()

    lateinit var interpolatorGroup: RadioGroup
    lateinit var accelerateBtn: MaterialRadioButton
    lateinit var accelerateDecelerateBtn: MaterialRadioButton
    lateinit var anticipateBtn: MaterialRadioButton
    lateinit var anticipateOvershootBtn: MaterialRadioButton
    lateinit var bounceBtn: MaterialRadioButton
    lateinit var decelerateBtn: MaterialRadioButton
    lateinit var fastOutSlowInBtn: MaterialRadioButton
    lateinit var fastOutLinearInBtn: MaterialRadioButton
    lateinit var linearOutSlowInBtn: MaterialRadioButton
    lateinit var linearBtn: MaterialRadioButton
    lateinit var overshootBtn: MaterialRadioButton
    lateinit var animSwitch: MaterialSwitch

    lateinit var alphaSlider: Slider
    lateinit var scaleXSlider: Slider
    lateinit var scaleYSlider: Slider
    lateinit var durationSlider: Slider
    lateinit var translationXSlider: Slider
    lateinit var translationYSlider: Slider
    lateinit var pivotXSlider: Slider
    lateinit var pivotYSlider: Slider
    lateinit var interpolatorValue: String

    lateinit var interpolatorValueKey: String

    lateinit var alphaSliderKey: Slider
    lateinit var scaleXSliderKey: Slider
    lateinit var scaleYSliderKey: Slider
    lateinit var durationSliderKey: Slider
    lateinit var translationXSliderKey: Slider
    lateinit var translationYSliderKey: Slider
    lateinit var listRadioKey: List<MaterialRadioButton>
    lateinit var slidersKey: List<Slider>
    lateinit var keyFrameSwitch: MaterialSwitch

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)


    }

    fun setUpViews(view: View){
        animSwitch = view.findViewById(R.id.animSwitch)
        alphaSlider = view.findViewById<Slider>(R.id.sliderAlpha)
        scaleXSlider = view.findViewById<Slider>(R.id.sliderScaleX)
        scaleYSlider = view.findViewById<Slider>(R.id.sliderScaleY)
        durationSlider = view.findViewById<Slider>(R.id.sliderDuration)
        translationXSlider = view.findViewById<Slider>(R.id.sliderTranslationX)
        translationYSlider = view.findViewById<Slider>(R.id.sliderTranslationY)
        pivotXSlider = view.findViewById<Slider>(R.id.sliderPivotX)
        pivotYSlider = view.findViewById<Slider>(R.id.sliderPivotY)

        interpolatorGroup = view.findViewById(R.id.interpolatorGroup)
        accelerateBtn = view.findViewById(R.id.accelerate)
        accelerateDecelerateBtn = view.findViewById(R.id.accelerateDecelerate)
        anticipateBtn = view.findViewById(R.id.anticipate)
        anticipateOvershootBtn = view.findViewById(R.id.anticipateOvershoot)
        bounceBtn = view.findViewById(R.id.bounce)
        decelerateBtn = view.findViewById(R.id.decelerate)
        fastOutSlowInBtn = view.findViewById(R.id.fastOutSlowIn)
        fastOutLinearInBtn = view.findViewById(R.id.fastOutLinearIn)
        linearOutSlowInBtn = view.findViewById(R.id.linearOutSlowIn)
        linearBtn = view.findViewById(R.id.linear)
        overshootBtn = view.findViewById(R.id.overshoot)

        val listRadio = listOf(
            accelerateBtn,
            accelerateDecelerateBtn,
            anticipateBtn,
            anticipateOvershootBtn,
            bounceBtn,
            decelerateBtn,
            fastOutSlowInBtn,
            fastOutLinearInBtn,
            linearOutSlowInBtn,
            linearBtn,
            overshootBtn)

        keyFrameSwitch = view.findViewById(R.id.keyFrameSwitch)
        val keyFrameLayout: LinearLayout = view.findViewById(R.id.layoutKeyFrame)



        val animations = viewModel.getAnimations(whatView = whatView)!!

        interpolatorGroup.check(when(animations.firstInterpolator){
            "AccelerateInterpolator" -> R.id.accelerate
            "AccelerateDecelerateInterpolator" -> R.id.accelerateDecelerate
            "AnticipateInterpolator" -> R.id.anticipate
            "AnticipateOvershootInterpolator" -> R.id.anticipateOvershoot
            "BounceInterpolator" -> R.id.bounce
            "DecelerateInterpolator" -> R.id.decelerate
            "FastOutSlowInInterpolator" -> R.id.fastOutSlowIn
            "FastOutLinearInInterpolator" -> R.id.fastOutLinearIn
            "LinearOutSlowInInterpolator" -> R.id.linearOutSlowIn
            "LinearInterpolator" -> R.id.linear
            "OvershootInterpolator" -> R.id.overshoot
            else -> 0
        })
        keyFrameSwitch.isChecked = animations.secondAnim
        keyFrameLayout.visibility = if (keyFrameSwitch.isChecked) View.VISIBLE else View.GONE

        interpolatorValue = when(interpolatorGroup.checkedRadioButtonId){
            R.id.accelerate ->{ "AccelerateInterpolator"}
            R.id.accelerateDecelerate ->{ "AccelerateDecelerateInterpolator"}
            R.id.anticipate ->{ "AnticipateInterpolator"}
            R.id.anticipateOvershoot ->{ "AnticipateOvershootInterpolator"}
            R.id.bounce ->{ "BounceInterpolator"}
            R.id.decelerate ->{ "DecelerateInterpolator"}
            R.id.fastOutSlowIn ->{ "FastOutSlowInInterpolator"}
            R.id.fastOutLinearIn ->{ "FastOutLinearInInterpolator"}
            R.id.linearOutSlowIn ->{ "LinearOutSlowInInterpolator"}
            R.id.linear ->{ "LinearInterpolator"}
            R.id.overshoot ->{ "OvershootInterpolator"}
            else -> ""
        }
        animSwitch.isChecked = animations.firstAnim
        animSwitch.text = if (animations.firstAnim) "Turned on" else "Turned off"
        alphaSlider.value = animations.firstAlpha
        scaleXSlider.value = animations.firstScaleX
        scaleYSlider.value = animations.firstScaleY
        durationSlider.value = animations.firstDuration.toFloat()
        translationXSlider.value = animations.firstTranslationX
        translationYSlider.value = animations.firstTranslationY
        pivotXSlider.value = animations.pivotX
        pivotYSlider.value = animations.pivotY

        val tvAlpha = view.findViewById<TextView>(R.id.tvAlpha)
        val tvScaleX = view.findViewById<TextView>(R.id.tvScaleX)
        val tvScaleY = view.findViewById<TextView>(R.id.tvScaleY)
        val tvDuration = view.findViewById<TextView>(R.id.tvDuration)
        val tvTranslationX = view.findViewById<TextView>(R.id.tvTranslationX)
        val tvTranslationY = view.findViewById<TextView>(R.id.tvTranslationY)
        val tvPivotX = view.findViewById<TextView>(R.id.tvPivotX)
        val tvPivotY = view.findViewById<TextView>(R.id.tvPivotY)

        val sliders = listOf(alphaSlider, scaleXSlider, scaleYSlider, durationSlider, translationXSlider, translationYSlider, pivotXSlider, pivotYSlider)
        val sliderTextViews = listOf(tvAlpha, tvScaleX, tvScaleY, tvDuration, tvTranslationX, tvTranslationY, tvPivotX, tvPivotY)

        for (i in listRadio){
            i.isEnabled = animSwitch.isChecked
        }
        for ((index, item) in sliders.withIndex()){
            item.isEnabled = animSwitch.isChecked
           sliderTextViews[index].text = if (sliderTextViews[index] == tvDuration ||
               sliderTextViews[index] == tvTranslationX ||
               sliderTextViews[index] == tvTranslationY) item.value.toInt().toString() else item.value.toString()
            item.addOnChangeListener { v, value, _ ->
                sliderTextViews[index].text = if (sliderTextViews[index] == tvDuration ||
                    sliderTextViews[index] == tvTranslationX ||
                    sliderTextViews[index] == tvTranslationY) value.toInt().toString() else value.toString()
                IsDataChanged.dataChanged()
            }
        }
        interpolatorGroup.setOnCheckedChangeListener {v, itemId ->
            interpolatorValue = when(itemId){
                R.id.accelerate ->{ "AccelerateInterpolator"}
                R.id.accelerateDecelerate ->{ "AccelerateDecelerateInterpolator"}
                R.id.anticipate ->{ "AnticipateInterpolator"}
                R.id.anticipateOvershoot ->{ "AnticipateOvershootInterpolator"}
                R.id.bounce ->{ "BounceInterpolator"}
                R.id.decelerate ->{ "DecelerateInterpolator"}
                R.id.fastOutSlowIn ->{ "FastOutSlowInInterpolator"}
                R.id.fastOutLinearIn ->{ "FastOutLinearInInterpolator"}
                R.id.linearOutSlowIn ->{ "LinearOutSlowInInterpolator"}
                R.id.linear ->{ "LinearInterpolator"}
                R.id.overshoot ->{ "OvershootInterpolator"}
                else -> ""
            }
            IsDataChanged.dataChanged()
            Log.d("dawdwa", "${itemId == R.id.accelerate}")
        }

        keyFrameSwitch.isEnabled = animSwitch.isChecked
        keyFrameSwitch.setOnCheckedChangeListener { v, state ->
            keyFrameLayout.visibility = if (state) View.VISIBLE else View.GONE
            IsDataChanged.dataChanged()

        }

        animSwitch.setOnCheckedChangeListener { v, state ->
            v.text = if (state) "Turned on" else "Turned off"
            for (i in sliders){
                i.isEnabled = v.isChecked
            }
            for (i in listRadio){
                i.isEnabled = v.isChecked
            }
            for (i in listRadioKey) i.isEnabled = v.isChecked
            for (i in slidersKey) i.isEnabled = v.isChecked
            keyFrameSwitch.isEnabled = state
            IsDataChanged.dataChanged()
        }
        setUpKeyFrame(view, animations)
    }

    fun setUpKeyFrame(view: View, data: AnimationSettings){
        alphaSliderKey = view.findViewById<Slider>(R.id.sliderAlphaKey)
        scaleXSliderKey = view.findViewById<Slider>(R.id.sliderScaleXKey)
        scaleYSliderKey = view.findViewById<Slider>(R.id.sliderScaleYKey)
        durationSliderKey = view.findViewById<Slider>(R.id.sliderDurationKey)
        translationXSliderKey = view.findViewById<Slider>(R.id.sliderTranslationXKey)
        translationYSliderKey = view.findViewById<Slider>(R.id.sliderTranslationYKey)

        val interpolatorGroupKey = view.findViewById<RadioGroup>(R.id.interpolatorGroupKey)
        val accelerateBtnKey = view.findViewById<MaterialRadioButton>(R.id.accelerateKey)
        val accelerateDecelerateBtnKey = view.findViewById<MaterialRadioButton>(R.id.accelerateDecelerateKey)
        val anticipateBtnKey = view.findViewById<MaterialRadioButton>(R.id.anticipateKey)
        val anticipateOvershootBtnKey = view.findViewById<MaterialRadioButton>(R.id.anticipateOvershootKey)
        val bounceBtnKey = view.findViewById<MaterialRadioButton>(R.id.bounceKey)
        val decelerateBtnKey = view.findViewById<MaterialRadioButton>(R.id.decelerateKey)
        val fastOutSlowInBtnKey = view.findViewById<MaterialRadioButton>(R.id.fastOutSlowInKey)
        val fastOutLinearInBtnKey = view.findViewById<MaterialRadioButton>(R.id.fastOutLinearInKey)
        val linearOutSlowInBtnKey = view.findViewById<MaterialRadioButton>(R.id.linearOutSlowInKey)
        val linearBtnKey = view.findViewById<MaterialRadioButton>(R.id.linearKey)
        val overshootBtnKey = view.findViewById<MaterialRadioButton>(R.id.overshootKey)

        listRadioKey = listOf(
            accelerateBtnKey,
            accelerateDecelerateBtnKey,
            anticipateBtnKey,
            anticipateOvershootBtnKey,
            bounceBtnKey,
            decelerateBtnKey,
            fastOutSlowInBtnKey,
            fastOutLinearInBtnKey,
            linearOutSlowInBtnKey,
            linearBtnKey,
            overshootBtnKey)

        interpolatorGroupKey.check(when(data.secondInterpolator){
            "AccelerateInterpolator" -> R.id.accelerateKey
            "AccelerateDecelerateInterpolator" -> R.id.accelerateDecelerateKey
            "AnticipateInterpolator" -> R.id.anticipateKey
            "AnticipateOvershootInterpolator" -> R.id.anticipateOvershootKey
            "BounceInterpolator" -> R.id.bounceKey
            "DecelerateInterpolator" -> R.id.decelerateKey
            "FastOutSlowInInterpolator" -> R.id.fastOutSlowInKey
            "FastOutLinearInInterpolator" -> R.id.fastOutLinearInKey
            "LinearOutSlowInInterpolator" -> R.id.linearOutSlowInKey
            "LinearInterpolator" -> R.id.linearKey
            "OvershootInterpolator" -> R.id.overshootKey
            else -> 0
        })

        interpolatorValueKey = when(interpolatorGroupKey.checkedRadioButtonId){
            R.id.accelerateKey ->{ "AccelerateInterpolator"}
            R.id.accelerateDecelerateKey ->{ "AccelerateDecelerateInterpolator"}
            R.id.anticipateKey ->{ "AnticipateInterpolator"}
            R.id.anticipateOvershootKey ->{ "AnticipateOvershootInterpolator"}
            R.id.bounceKey ->{ "BounceInterpolator"}
            R.id.decelerateKey ->{ "DecelerateInterpolator"}
            R.id.fastOutSlowInKey ->{ "FastOutSlowInInterpolator"}
            R.id.fastOutLinearInKey ->{ "FastOutLinearInInterpolator"}
            R.id.linearOutSlowInKey ->{ "LinearOutSlowInInterpolator"}
            R.id.linearKey ->{ "LinearInterpolator"}
            R.id.overshootKey ->{ "OvershootInterpolator"}
            else -> ""
        }

        alphaSliderKey.value = data.secondAlpha
        scaleXSliderKey.value = data.secondScaleX
        scaleYSliderKey.value = data.secondScaleY
        durationSliderKey.value = data.secondDuration.toFloat()
        translationXSliderKey.value = data.secondTranslationX
        translationYSliderKey.value = data.secondTranslationY

        val tvAlphaKey = view.findViewById<TextView>(R.id.tvAlphaKey)
        val tvScaleXKey = view.findViewById<TextView>(R.id.tvScaleXKey)
        val tvScaleYKey = view.findViewById<TextView>(R.id.tvScaleYKey)
        val tvDurationKey = view.findViewById<TextView>(R.id.tvDurationKey)
        val tvTranslationXKey = view.findViewById<TextView>(R.id.tvTranslationXKey)
        val tvTranslationYKey = view.findViewById<TextView>(R.id.tvTranslationYKey)

        slidersKey = listOf(
            alphaSliderKey,
            scaleXSliderKey,
            scaleYSliderKey,
            durationSliderKey,
            translationXSliderKey,
            translationYSliderKey)

        val sliderTextViewsKey = listOf(
            tvAlphaKey,
            tvScaleXKey,
            tvScaleYKey,
            tvDurationKey,
            tvTranslationXKey,
            tvTranslationYKey)

        for (i in listRadioKey){
            i.isEnabled = animSwitch.isChecked
        }

        for ((index, item) in slidersKey.withIndex()){
            item.isEnabled = animSwitch.isChecked
            sliderTextViewsKey[index].text = if (
                sliderTextViewsKey[index] == tvDurationKey ||
                sliderTextViewsKey[index] == tvTranslationXKey ||
                sliderTextViewsKey[index] == tvTranslationYKey) item.value.toInt().toString() else item.value.toString()
            item.addOnChangeListener { v, value, _ ->
                sliderTextViewsKey[index].text = if (
                    sliderTextViewsKey[index] == tvDurationKey ||
                    sliderTextViewsKey[index] == tvTranslationXKey ||
                    sliderTextViewsKey[index] == tvTranslationYKey) value.toInt().toString() else value.toString()
                IsDataChanged.dataChanged()
            }
        }

        interpolatorGroupKey.setOnCheckedChangeListener {v, itemId ->
            interpolatorValueKey = when(itemId){
                R.id.accelerateKey ->{ "AccelerateInterpolator"}
                R.id.accelerateDecelerateKey ->{ "AccelerateDecelerateInterpolator"}
                R.id.anticipateKey ->{ "AnticipateInterpolator"}
                R.id.anticipateOvershootKey ->{ "AnticipateOvershootInterpolator"}
                R.id.bounceKey ->{ "BounceInterpolator"}
                R.id.decelerateKey ->{ "DecelerateInterpolator"}
                R.id.fastOutSlowInKey ->{ "FastOutSlowInInterpolator"}
                R.id.fastOutLinearInKey ->{ "FastOutLinearInInterpolator"}
                R.id.linearOutSlowInKey ->{ "LinearOutSlowInInterpolator"}
                R.id.linearKey ->{ "LinearInterpolator"}
                R.id.overshootKey ->{ "OvershootInterpolator"}
                else -> ""
            }
            IsDataChanged.dataChanged()
        }

    }

    fun saveData(){
        viewModel.saveAnimation(
            whatView,
            animSwitch.isChecked,
            alphaSlider.value,
            scaleXSlider.value,
            scaleYSlider.value,
            durationSlider.value.toLong(),
            translationXSlider.value,
            translationYSlider.value,
            pivotXSlider.value,
            pivotYSlider.value,
            interpolatorValue,
            keyFrameSwitch.isChecked,
            alphaSliderKey.value,
            scaleXSliderKey.value,
            scaleYSliderKey.value,
            durationSliderKey.value.toLong(),
            translationXSliderKey.value,
            translationYSliderKey.value,
            interpolatorValueKey)
    }

}