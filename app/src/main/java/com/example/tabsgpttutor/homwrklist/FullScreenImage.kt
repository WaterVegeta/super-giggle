package com.example.tabsgpttutor.homwrklist

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.viewpager2.widget.ViewPager2
import com.example.tabsgpttutor.R
import com.google.android.material.appbar.AppBarLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.MaterialToolbar

class FullScreenImage : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var appBar: AppBarLayout
    var isAppBarVis: Boolean = false
    lateinit var textOfImage: TextView
    lateinit var cardView: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        window.navigationBarColor = Color.TRANSPARENT
//        window.statusBarColor = Color.TRANSPARENT

        hideSystemBars()
        setContentView(R.layout.activity_full_screen_image)

        appBar= findViewById(R.id.appBar)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolBarImage)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }


        textOfImage= findViewById(R.id.textImage)
        cardView = findViewById(R.id.cardViewImage)

        viewPager = findViewById<ViewPager2>(R.id.imageViewPager)
        val imageUris = intent.getStringArrayExtra("imageUris")!!.toList()
        Log.v("recived uri", "recived: $imageUris")
        val startPosition = intent.getIntExtra("startPosition", 0)
        val homework = intent.getStringExtra("homework")
        val lesson = intent.getStringExtra("lesson")
        textOfImage.text = (startPosition+1).toString() + " of " + (imageUris.size.toString())
        toolbar.title = homework
        toolbar.subtitle = lesson

        viewPager.adapter = FullScreenImageAdapter(imageUris,
//            listener = object : FullScreenImageAdapter.OnImageTap{
//                override fun onSingleTap() {
//                    appBar.visibility = if (appBar.isVisible) View.GONE else View.VISIBLE
//                }
//
//                override fun onDoubleTap() {
//                    TODO("Not yet implemented")
//                }
//            }
        )
        viewPager.setCurrentItem(startPosition, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setUpPhotoViewTap(viewPager, 0)
                textOfImage.text = (position+1).toString() + " of " + (imageUris.size.toString())

            }
        })
//        viewPager.setOnTouchListener { _, e ->
//            setUpPhotoViewTap()
//            false
//        }
        viewPager.post {
            setUpPhotoViewTap(viewPager, 0)
            appBar.translationY = -appBar.height.toFloat()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    fun setUpPhotoViewTap(viewPager: ViewPager2, position: Int){
        val rv = viewPager.getChildAt(position) as RecyclerView
        val currentHolder = rv.findViewHolderForAdapterPosition(viewPager.currentItem)

        currentHolder?.itemView?.findViewById<PhotoView>(R.id.fullImage)?.setOnPhotoTapListener {_, _, _ ->
            if (isAppBarVis) hideAppBar() else showAppBar()
            isAppBarVis = !isAppBarVis
        }
    }

    fun hideAppBar(){
        appBar.animate().apply {
            setDuration(250)
            setInterpolator(AccelerateDecelerateInterpolator())
            translationY(-appBar.height.toFloat())
        }
        cardView.animate().apply {
            setDuration(250)
            setInterpolator(AccelerateDecelerateInterpolator())
            translationY(-appBar.height.toFloat()-cardView.height.toFloat())
        }
    }

    fun showAppBar(){
        appBar.animate().apply {
            setDuration(250)
            setInterpolator(AccelerateDecelerateInterpolator())
            translationY(0f)
        }
        cardView.animate().apply {
            setDuration(250)
            setInterpolator(AccelerateDecelerateInterpolator())
            translationY(0f)
        }
    }

    fun showSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.insetsController?.show(WindowInsets.Type.systemBars())
    }

    fun hideSystemBars(){
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.insetsController?.let {
            // Hide both status bar and navigation bar
            it.hide(WindowInsets.Type.systemBars())
            // Make bars appear transiently when swiped
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

}