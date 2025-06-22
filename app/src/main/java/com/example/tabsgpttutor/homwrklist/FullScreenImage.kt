package com.example.tabsgpttutor.homwrklist

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.viewpager2.widget.ViewPager2
import com.example.tabsgpttutor.R
import com.google.android.material.appbar.AppBarLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.HwViewModel
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.MaterialToolbar

class FullScreenImage : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var appBar: AppBarLayout
    var isAppBarVis: Boolean = false
    lateinit var textOfImage: TextView
    lateinit var toolbar: MaterialToolbar
    val viewModel : HwViewModel by viewModels()
    lateinit var imageUris: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        window.navigationBarColor = Color.TRANSPARENT
//        window.statusBarColor = Color.TRANSPARENT

        hideSystemBars()
        setContentView(R.layout.activity_full_screen_image)

        appBar= findViewById(R.id.appBar)
        toolbar = findViewById<MaterialToolbar>(R.id.toolBarImage)
//        setSupportActionBar(toolbar)
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//        }


        textOfImage= findViewById(R.id.textImage)

        viewPager = findViewById<ViewPager2>(R.id.imageViewPager)
        imageUris = intent.getStringArrayExtra("imageUris")!!.toList()
        Log.v("recived uri", "recived: $imageUris")
        val startPosition = intent.getIntExtra("startPosition", 0)
        val homework = intent.getStringExtra("homework")
        val lesson = intent.getStringExtra("lesson")
        val ids = intent.getStringArrayExtra("ids")!!.toList()
        textOfImage.text = (startPosition+1).toString() + " of " + (imageUris.size.toString())
        toolbar.title = homework
        toolbar.subtitle = lesson

        viewPager.adapter = FullScreenImageAdapter(imageUris)
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
            textOfImage.translationY =  -appBar.height.toFloat()-textOfImage.height.toFloat() -50f
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.share ->{
                    val currentUri = Uri.parse(imageUris[viewPager.currentItem]) // Use parse, not toUri()

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, currentUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val chooserIntent = Intent.createChooser(shareIntent, "Share Image").apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

// Grant read permission to all target apps
                    val resInfoList = packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    for (info in resInfoList) {
                        grantUriPermission(
                            info.activityInfo.packageName,
                            currentUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }

                    startActivity(chooserIntent)
                    true
                }
                R.id.delete ->{
                    Toast.makeText(this, "delete", Toast.LENGTH_LONG).show()
                    val currentUri = Uri.parse(imageUris[viewPager.currentItem])
                    val currentId = ids[viewPager.currentItem]
                    viewModel.deleteCurImage(currentUri, currentId)
                    imageUris.minus(currentUri)
                    ids.minus(currentId)
                    viewPager.adapter?.notifyItemRemoved(viewPager.currentItem)
                    true
                }
                else -> false
            }
        }

    }

//    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return true
//    }
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
        textOfImage.animate().apply {
            setDuration(250)
            setInterpolator(AccelerateDecelerateInterpolator())
            translationY(-appBar.height.toFloat()-textOfImage.height.toFloat() -50f)
        }
    }

    fun showAppBar(){
        appBar.animate().apply {
            setDuration(250)
            setInterpolator(AccelerateDecelerateInterpolator())
            translationY(0f)
        }
        textOfImage.animate().apply {
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