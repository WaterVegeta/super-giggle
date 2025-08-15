package com.example.task_king.widget

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
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
import androidx.core.view.setPadding
import com.example.task_king.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider

class WidgetSettings : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    lateinit var alphaSlider: Slider

    lateinit var toolbar: MaterialToolbar

    lateinit var cancelBtn: MaterialButton
    lateinit var saveBtn: MaterialButton

    lateinit var widBackground: FrameLayout
    lateinit var widInnerBox: FrameLayout

    var alpha: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        setResult(RESULT_CANCELED)

        setContentView(R.layout.widget_settings)
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        widgetId()

        setUpViews()

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        loadData()

//        val wallpaperManager = WallpaperManager.getInstance(this)
//        val wallpaperDrawable = wallpaperManager.getDraw

//        cardView.setBackgroundDrawable(wallpaperDrawable)

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

        cancelBtn = findViewById<MaterialButton>(R.id.cancel_button)
        saveBtn = findViewById<MaterialButton>(R.id.save_button)

        widBackground = findViewById(R.id.wid_background)
        widInnerBox = findViewById(R.id.list_box)

        alphaSlider = findViewById(R.id.alpha_slider)

        alphaSlider.addOnChangeListener { v, value, _ ->
            alpha = value
            widInnerBox.alpha = alpha
            widBackground.alpha = alpha
        }

        saveBtn.setOnClickListener {
            WidgetPreferences.saveAlpha(this, appWidgetId, alpha)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            DynamicWidProvider.updateWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
        cancelBtn.setOnClickListener {
            finish()
        }
    }

    fun loadData(){
        val loadAlpha = WidgetPreferences.getAlpha(this, appWidgetId)

        alphaSlider.value = loadAlpha

//        changeMode(mode)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
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
        Log.i("finished", "yes")
        super.onDestroy()
    }
}