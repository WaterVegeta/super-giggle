package com.example.tabsgpttutor.widget

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tabsgpttutor.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider

class WidgetSettings : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    lateinit var colorView: LinearLayout

    lateinit var hueSlider: Slider
    lateinit var satSlider: Slider
    lateinit var valueSlider: Slider
    lateinit var alphaSlider: Slider

    var hue : Float = 0f
    var saturation: Float = 0f
    var hueValue: Float = 0f
    var alpha: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_settings)

        colorView = findViewById(R.id.color_view)
        hueSlider = findViewById(R.id.hue_slider)
        satSlider = findViewById(R.id.saturation_slider)
        valueSlider = findViewById(R.id.value_slider)
        alphaSlider = findViewById(R.id.alpha_slider)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

//        val sliders = listOf(
//            hueSlider,
//            satSlider,
//            valueSlider,
//            alphaSlider
//        )
//        val hvs = listOf(
//            hue,
//            saturation,
//            value,
//            alpha
//        )
//
//        for ((index, i) in sliders.withIndex()){
//            i.addOnChangeListener { v, value, _ ->
//
//            }
//        }


        hue = hueSlider.value
        saturation = satSlider.value
        hueValue = valueSlider.value
        alpha = alphaSlider.value
        changeViewColor(hue, saturation, hueValue, alpha)

        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.builtInDrawable

        val layout = findViewById<ConstraintLayout>(R.id.main)
        layout.background = wallpaperDrawable

        hueSlider.addOnChangeListener { v, value, _ ->
            hue = value
            changeViewColor(hue, saturation, hueValue, alpha)
        }

        satSlider.addOnChangeListener { v, value, _ ->
            saturation = value
            changeViewColor(hue, saturation, hueValue, alpha)
        }

        valueSlider.addOnChangeListener { v, value, _ ->
            hueValue = value
            changeViewColor(hue, saturation, hueValue, alpha)
        }

        alphaSlider.addOnChangeListener { v, value, _ ->
            alpha = value
            changeViewColor(hue, saturation, hueValue, alpha)
        }

        val intent = intent
        val extras = intent.extras

        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }


        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        WidgetPreferences.saveColor(this, appWidgetId, adjustHueSA(hue, saturation, hueValue, alpha))

        val appWidgetManager = AppWidgetManager.getInstance(this)
        DynamicWidProvider.updateWidget(this, appWidgetManager, appWidgetId)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
        return super.onSupportNavigateUp()
    }

    fun changeViewColor(hue: Float,
                        saturation: Float,
                        hueValue: Float,
                        alpha: Float){
        colorView.setBackgroundColor(adjustHueSA(hue, saturation, hueValue, alpha))
    }

    fun adjustHueSA(newHue: Float, newSaturation: Float, newValue: Float, newAlpha: Float): Int {
        // saturation 0 == white, 1 == pure color, 2 change to value == black
        val hsv = floatArrayOf(newHue, newSaturation, newValue)

//        hsv[0] = newHue       // 0..360
//        hsv[1] = newSaturation // 0..1

        val rgb = Color.HSVToColor(hsv)
        return Color.argb((newAlpha * 255).toInt(), Color.red(rgb), Color.green(rgb), Color.blue(rgb))
    }
}