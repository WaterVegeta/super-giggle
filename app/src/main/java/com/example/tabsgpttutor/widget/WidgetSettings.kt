package com.example.tabsgpttutor.widget

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.tabsgpttutor.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider

class WidgetSettings : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    lateinit var colorView: LinearLayout

    lateinit var hueSlider: Slider
    lateinit var satSlider: Slider
    lateinit var valueSlider: Slider
    lateinit var alphaSlider: Slider

    lateinit var textChangeButton: MaterialButton

    lateinit var btnSystem: MaterialButton
    lateinit var btnDay: MaterialButton
    lateinit var btnNight: MaterialButton
    lateinit var btnDynamic: MaterialButton
    lateinit var btnCustom: MaterialButton

    lateinit var dateText: TextView
    lateinit var weekText: TextView

//    val textList = listOf(dateText, weekText)

    val SYSTEM = 0
    val DAY = 1
    val NIGHT = 2
    val DYNAMIC = 3
    val CUSTOM = 69

    var currentMode = SYSTEM

    var hue : Float = 0f
    var saturation: Float = 0f
    var hueValue: Float = 0f
    var alpha: Float = 0f
    var isTextBlack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.widget_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
        }

        colorView = findViewById(R.id.color_view)
        hueSlider = findViewById(R.id.hue_slider)
        satSlider = findViewById(R.id.saturation_slider)
        valueSlider = findViewById(R.id.value_slider)
        alphaSlider = findViewById(R.id.alpha_slider)

        btnSystem = findViewById(R.id.btnSystem)
        btnDay = findViewById(R.id.btnDay)
        btnNight = findViewById(R.id.btnNight)
        btnDynamic = findViewById(R.id.btnDynamic)
        btnCustom = findViewById(R.id.btnCustom)

        btnSystem.setOnClickListener {
            currentMode = SYSTEM
            changeMode()
        }

        btnDay.setOnClickListener {
            currentMode = DAY
            changeMode()
        }

        btnNight.setOnClickListener {
            currentMode = NIGHT
            changeMode()
        }

        btnDynamic.setOnClickListener {
            currentMode = DYNAMIC
            changeMode()
        }

        btnCustom.setOnClickListener {
            currentMode = CUSTOM
            changeMode()
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        textChangeButton = findViewById(R.id.change_txt_color)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }


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

        weekText = findViewById(R.id.weekText)
        dateText = findViewById(R.id.dateText)

        textChangeButton.setOnClickListener {
            isTextBlack = !isTextBlack
            if (isTextBlack){
                weekText.setTextColor(Color.BLACK)
                dateText.setTextColor(Color.BLACK)
            }
            else{
                weekText.setTextColor(Color.WHITE)
                dateText.setTextColor(Color.WHITE)
            }
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

    fun changeMode(){
        val customList = listOf(
            hueSlider,
            satSlider,
            alphaSlider,
            valueSlider,
            textChangeButton
        )
        if (currentMode == CUSTOM){
            for (i in customList){
                i.isVisible = true
            }
        }
        else{
            for (i in customList){
                i.isVisible = false
            }
            when(currentMode){
                SYSTEM ->{

                }
                DAY ->{
                    colorView.setBackgroundColor(Color.WHITE)
                    dateText.setTextColor(Color.BLACK)
                    weekText.setTextColor(Color.BLACK)
                }
                NIGHT ->{
                    colorView.setBackgroundColor(Color.BLACK)
                    dateText.setTextColor(Color.WHITE)
                    weekText.setTextColor(Color.WHITE)
                }
                DYNAMIC ->{
                    colorView.setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary,
                        Color.RED))
                    dateText.setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSecondary,
                        Color.BLACK))
                    weekText.setTextColor(MaterialColors.getColor(this, R.attr.colorOnTertiary,
                        Color.BLACK))
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val textColor = if (isTextBlack) Color.BLACK else Color.WHITE
        WidgetPreferences.saveColor(
            this, appWidgetId, adjustHueSA(hue, saturation, hueValue, alpha),
            textColor
        )

        val appWidgetManager = AppWidgetManager.getInstance(this)
        DynamicWidProvider.updateWidget(this, appWidgetManager, appWidgetId)

        val idsTrans = appWidgetManager.getAppWidgetIds(ComponentName(this,
            DynamicWidProvider::class.java))
        appWidgetManager.notifyAppWidgetViewDataChanged(idsTrans, R.id.listView)

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