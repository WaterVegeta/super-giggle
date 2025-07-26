package com.example.task_king.widget

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.task_king.R
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
    lateinit var toolbar: MaterialToolbar

    lateinit var btnSystem: MaterialButton
    lateinit var btnDay: MaterialButton
    lateinit var btnNight: MaterialButton
    lateinit var btnDynamic: MaterialButton
    lateinit var btnCustom: MaterialButton

    lateinit var dateText: TextView
    lateinit var weekText: TextView

    lateinit var hueText: TextView
    lateinit var saturText: TextView
    lateinit var valueText: TextView

//    val textList = listOf(dateText, weekText)

    var curSurface = Color.TRANSPARENT
    var curTextColor = Color.WHITE

    val SYSTEM = 0
    val DAY = 1
    val NIGHT = 2
    val DYNAMIC = 3
    val CUSTOM = 69

    var currentMode = DAY

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
        widgetId()

        setUpViews()
        setUpSliders()

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        loadData()
        hue = hueSlider.value
        saturation = satSlider.value
        hueValue = valueSlider.value
        alpha = alphaSlider.value

        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.builtInDrawable

        val layout = findViewById<ConstraintLayout>(R.id.main)
        layout.background = wallpaperDrawable

        textChangeButton.setOnClickListener {
            if (curTextColor == Color.BLACK){
                curTextColor = Color.WHITE
                weekText.setTextColor(curTextColor)
                dateText.setTextColor(curTextColor)
            }
            else{
                curTextColor = Color.BLACK
                weekText.setTextColor(curTextColor)
                dateText.setTextColor(curTextColor)
            }
        }



    }

    fun widgetId(){
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

    fun setUpViews(){
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        weekText = findViewById(R.id.weekText)
        dateText = findViewById(R.id.dateText)

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

        hueText = findViewById(R.id.tvHue)
        saturText = findViewById(R.id.tvSaturation)
        valueText = findViewById(R.id.tvValue)

        textChangeButton = findViewById(R.id.change_txt_color)

        btnSystem.setOnClickListener { v ->
            changeMode(SYSTEM)
        }

        btnDay.setOnClickListener {v ->
            changeMode(DAY)
        }

        btnNight.setOnClickListener {v ->
            changeMode(NIGHT)
        }

        btnDynamic.setOnClickListener {v ->
            changeMode(DYNAMIC)
        }

        btnCustom.setOnClickListener {v ->
            changeMode(CUSTOM)
        }
    }

    fun setUpSliders(){
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
            changeAlpha(alpha)
        }
    }

    fun loadData(){
        val mode = WidgetPreferences.getMode(this, appWidgetId)
        val surface = WidgetPreferences.loadColor(this, appWidgetId)
        val loadAlpha = WidgetPreferences.getAlpha(this, appWidgetId)
        val textColor = WidgetPreferences.loadTextColor(this, appWidgetId)

        curTextColor = textColor
        curSurface = surface
        alphaSlider.value = loadAlpha

        val array = FloatArray(3)
        Color.colorToHSV(surface, array)
        hueSlider.value = array[0]
        satSlider.value = array[1]
        valueSlider.value = array[2]

//        changeMode(mode)
        when(mode){
            SYSTEM -> btnSystem.performClick()
            DAY -> btnDay.performClick()
            NIGHT -> btnNight.performClick()
            DYNAMIC -> btnDynamic.performClick()
            CUSTOM -> btnCustom.performClick()
        }
    }

    fun changeAlpha(alpha: Float){
        val color = Color.argb((255 * alpha).toInt(), curSurface.red, curSurface.green, curSurface.blue)
        curSurface = color
        val background = colorView.background
        background.setTint(color)
    }

    fun changeMode(newMode: Int){
        val customList = listOf(
            hueSlider,
            satSlider,
            valueSlider,
            textChangeButton,
            hueText,
            saturText,
            valueText
        )
        when(newMode){
            SYSTEM -> btnSystem.isSelected = true
            DAY -> btnDay.isSelected = true
            NIGHT -> btnNight.isSelected = true
            DYNAMIC -> btnDynamic.isSelected = true
            CUSTOM -> btnCustom.isSelected = true
        }
        if (currentMode != newMode){
            when(currentMode){
                SYSTEM -> btnSystem.isSelected = false
                DAY -> btnDay.isSelected = false
                NIGHT -> btnNight.isSelected = false
                DYNAMIC -> btnDynamic.isSelected = false
                CUSTOM -> btnCustom.isSelected = false
            }
        }

        currentMode = newMode

        if (currentMode == CUSTOM){
            for (i in customList){
                i.isVisible = true
                changeViewColor(hue, saturation, hueValue, alpha)
                dateText.setTextColor(curTextColor)
                weekText.setTextColor(curTextColor)
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
                    curSurface = Color.WHITE
                    val background = colorView.background
                    background.setTint(Color.WHITE)
                    curTextColor = Color.BLACK
                    dateText.setTextColor(Color.BLACK)
                    weekText.setTextColor(Color.BLACK)
                    changeAlpha(alpha)
                }

                NIGHT ->{
                    curSurface = Color.BLACK
                    val background = colorView.background
                    background.setTint(Color.BLACK)
                    curTextColor = Color.WHITE
                    dateText.setTextColor(Color.WHITE)
                    weekText.setTextColor(Color.WHITE)
                    changeAlpha(alpha)
                }

                DYNAMIC ->{
                    val textColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface,
                        Color.BLACK)
                    val surfaceColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondaryContainer,
                        Color.RED)
                    val lsit = FloatArray(3)
                    Color.colorToHSV(surfaceColor, lsit)
                    lsit[2] = lsit[2] -0.07f
                    val rgb= Color.HSVToColor(lsit)

                    curSurface = rgb
                    curTextColor = textColor

                    val background = colorView.background
                    background.setTint(rgb)
                    dateText.setTextColor(textColor)
                    weekText.setTextColor(textColor)
                    changeAlpha(alpha)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    fun changeViewColor(hue: Float,
                        saturation: Float,
                        hueValue: Float,
                        alpha: Float){
        val newColor = adjustHueSA(hue, saturation, hueValue)
        curSurface = newColor
        changeAlpha(alpha)


//        colorView.setBackgroundColor(
//            adjustHueSA(hue, saturation, hueValue, alpha)
//        )
    }

    fun adjustHueSA(newHue: Float, newSaturation: Float, newValue: Float): Int {
        // saturation 0 == white, 1 == pure color, 2 change to value == black
        val hsv = floatArrayOf(newHue, newSaturation, newValue)

//        hsv[0] = newHue       // 0..360
//        hsv[1] = newSaturation // 0..1

        val rgb = Color.HSVToColor(hsv)
        return Color.argb(255, Color.red(rgb), Color.green(rgb), Color.blue(rgb))
    }

    override fun onStop() {
        finish()
        super.onStop()
    }

    override fun onDestroy() {
        if(appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID){
            WidgetPreferences.saveColor(
                this, appWidgetId,
                curSurface,
                curTextColor,
                currentMode,
                alphaSlider.value
            )

            val appWidgetManager = AppWidgetManager.getInstance(this)
            DynamicWidProvider.updateWidget(this, appWidgetManager, appWidgetId)

            val idsTrans = appWidgetManager.getAppWidgetIds(ComponentName(this,
                DynamicWidProvider::class.java))
            appWidgetManager.notifyAppWidgetViewDataChanged(idsTrans, R.id.listView)

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            Log.i("finished", "yes")
        }

        setResult(RESULT_CANCELED)

        super.onDestroy()
    }
}