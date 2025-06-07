package com.example.tabsgpttutor.animation_pref

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tabsgpttutor.HwViewModel
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.AnimationSettings
import com.example.tabsgpttutor.schedule_change.IsDataChanged
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.slider.Slider

class KeyFrameScheduleFragment : Fragment(R.layout.frag_between_keyframe) {


    lateinit var interpolatorValue: String
    private val viewModel: HwViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animSwitch = view.findViewById<MaterialSwitch>(R.id.keyFrameSwitch)
        val alphaSlider = view.findViewById<Slider>(R.id.sliderAlphaKey)
        val scaleXSlider = view.findViewById<Slider>(R.id.sliderScaleXKey)
        val scaleYSlider = view.findViewById<Slider>(R.id.sliderScaleYKey)
        val durationSlider = view.findViewById<Slider>(R.id.sliderDurationKey)
        val translationXSlider = view.findViewById<Slider>(R.id.sliderTranslationXKey)
        val translationYSlider = view.findViewById<Slider>(R.id.sliderTranslationYKey)

        val interpolatorGroup = view.findViewById<RadioGroup>(R.id.interpolatorGroupKey)
        val accelerateBtn = view.findViewById<MaterialRadioButton>(R.id.accelerateKey)
        val accelerateDecelerateBtn =
            view.findViewById<MaterialRadioButton>(R.id.accelerateDecelerateKey)
        val anticipateBtn = view.findViewById<MaterialRadioButton>(R.id.anticipateKey)
        val anticipateOvershootBtn =
            view.findViewById<MaterialRadioButton>(R.id.anticipateOvershootKey)
        val bounceBtn = view.findViewById<MaterialRadioButton>(R.id.bounceKey)
        val decelerateBtn = view.findViewById<MaterialRadioButton>(R.id.decelerateKey)
        val fastOutSlowInBtn = view.findViewById<MaterialRadioButton>(R.id.fastOutSlowInKey)
        val fastOutLinearInBtn = view.findViewById<MaterialRadioButton>(R.id.fastOutLinearInKey)
        val linearOutSlowInBtn = view.findViewById<MaterialRadioButton>(R.id.linearOutSlowInKey)
        val linearBtn = view.findViewById<MaterialRadioButton>(R.id.linearKey)
        val overshootBtn = view.findViewById<MaterialRadioButton>(R.id.overshootKey)

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
            overshootBtn
        )
        var data = viewModel.animationRecieved

        if (data != null){

            interpolatorGroup.check(when(data.secondInterpolator){
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

            interpolatorValue = when(interpolatorGroup.checkedRadioButtonId){
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

            animSwitch.isChecked = IsDataChanged.switchState
            animSwitch.text = if (data.secondAnim) "Turned on" else "Turned off"
            alphaSlider.value = data.secondAlpha
            scaleXSlider.value = data.secondScaleX
            scaleYSlider.value = data.secondScaleY
            durationSlider.value = data.secondDuration.toFloat()
            translationXSlider.value = data.secondTranslationX
            translationYSlider.value = data.secondTranslationY
        }

        val tvAlpha = view.findViewById<TextView>(R.id.tvAlphaKey)
        val tvScaleX = view.findViewById<TextView>(R.id.tvScaleXKey)
        val tvScaleY = view.findViewById<TextView>(R.id.tvScaleYKey)
        val tvDuration = view.findViewById<TextView>(R.id.tvDurationKey)
        val tvTranslationX = view.findViewById<TextView>(R.id.tvTranslationXKey)
        val tvTranslationY = view.findViewById<TextView>(R.id.tvTranslationYKey)

        val sliders = listOf(
            alphaSlider,
            scaleXSlider,
            scaleYSlider,
            durationSlider,
            translationXSlider,
            translationYSlider
        )
        val sliderTextViews = listOf(
            tvAlpha,
            tvScaleX,
            tvScaleY,
            tvDuration,
            tvTranslationX,
            tvTranslationY
        )

        for (i in listRadio) {
            i.isEnabled = animSwitch.isChecked
        }
        for ((index, item) in sliders.withIndex()) {
            item.isEnabled = animSwitch.isChecked
            sliderTextViews[index].text = if (sliderTextViews[index] == tvDuration ||
                sliderTextViews[index] == tvTranslationX ||
                sliderTextViews[index] == tvTranslationY
            ) item.value.toInt().toString() else item.value.toString()
            item.addOnChangeListener { v, value, _ ->
                sliderTextViews[index].text = if (sliderTextViews[index] == tvDuration ||
                    sliderTextViews[index] == tvTranslationX ||
                    sliderTextViews[index] == tvTranslationY
                ) value.toInt().toString() else value.toString()
                IsDataChanged.dataChanged()
            }
        }

        interpolatorGroup.setOnCheckedChangeListener {v, itemId ->
            interpolatorValue = when(itemId){
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

        animSwitch.setOnCheckedChangeListener { v, state ->
            v.text = if (state) "Turned on" else "Turned off"
            for (i in sliders){
                i.isEnabled = v.isChecked
            }
            for (i in listRadio){
                i.isEnabled = v.isChecked
            }
            IsDataChanged.dataChanged()
        }
    }


    override fun onDetach() {
        super.onDetach()
        Log.d("Detach", "detach anim 2")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Stop", "stop anim 2")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Destroy", "destroy anim 2")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DestroyView", "destroyview anim 2")
    }

}