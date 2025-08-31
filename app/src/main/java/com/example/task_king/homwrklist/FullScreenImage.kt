package com.example.task_king.homwrklist

import android.app.SharedElementCallback
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.transition.Transition
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.example.task_king.R
import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.task_king.HwViewModel
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.delay

class FullScreenImage : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var appBar: AppBarLayout
    var isAppBarVis: Boolean = false
    lateinit var textOfImage: TextView
    lateinit var toolbar: MaterialToolbar
    val viewModel : HwViewModel by viewModels()
    lateinit var imageUris: MutableList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        window.navigationBarColor = Color.TRANSPARENT
//        window.statusBarColor = Color.TRANSPARENT

//        hideSystemBars()

        setContentView(R.layout.activity_full_screen_image)


        val transition = window.sharedElementEnterTransition
        transition?.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
            }

            override fun onTransitionStart(transition: Transition) {
                showSystemBars()
            }
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
        })
//        setEnterSharedElementCallback(object : SharedElementCallback() {
//            override fun onSharedElementEnd(
//                sharedElementNames: List<String?>?,
//                sharedElements: List<View?>?,
//                sharedElementSnapshots: List<View?>?
//            ) {
//                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
//                hideSystemBars()
//            }
//        })

//        setExitSharedElementCallback(object : SharedElementCallback() {
//            override fun onSharedElementStart(
//                sharedElementNames: List<String?>?,
//                sharedElements: List<View?>?,
//                sharedElementSnapshots: List<View?>?
//            ) {
//                super.onSharedElementStart(
//                    sharedElementNames,
//                    sharedElements,
//                    sharedElementSnapshots
//                )
//                showSystemBars()
//            }
//        })
        appBar = findViewById(R.id.appBar)
        toolbar = findViewById<MaterialToolbar>(R.id.toolBarImage)
//        setSupportActionBar(toolbar)
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            viewPager.updatePadding(
//                top = systemBars.top
//            )
//            appBar.updatePadding(
//                top = systemBars.top
//            )
//
//            insets
//        }

        textOfImage= findViewById(R.id.textImage)

        viewPager = findViewById<ViewPager2>(R.id.imageViewPager)

        ViewCompat.setTransitionName(viewPager, "image")

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. This solution sets
            // only the bottom, left, and right dimensions, but you can apply whichever
            // insets are appropriate to your layout. You can also update the view padding
            // if that's more appropriate.

            v.setPadding(insets.left, insets.top, insets.right, 0)
//            v.updateLayoutParams<MarginLayoutParams> {
//                topMargin = insets.top
//            }
            // Return CONSUMED if you don't want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        hideAppBar()
        imageUris = intent.getStringArrayExtra("imageUris")!!.toMutableList()
//        Log.v("recived uri", "recived: $imageUris")

        val startPosition = intent.getIntExtra("startPosition", 0)
        val homework = intent.getStringExtra("homework")
        val lesson = intent.getStringExtra("lesson")
        val ids = intent.getStringArrayExtra("ids")!!.toMutableList()

        textOfImage.text = (startPosition+1).toString() + getString(R.string.of) + (imageUris.size.toString())
        toolbar.title = homework
        toolbar.subtitle = lesson

        viewPager.adapter = FullScreenImageAdapter(imageUris)
        viewPager.setCurrentItem(startPosition, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setUpPhotoViewTap(viewPager, 0)
                textOfImage.text = (position+1).toString() + getString(R.string.of) + (imageUris.size.toString())

            }
        })

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
                    val position = viewPager.currentItem
                    val currentUri = Uri.parse(imageUris[position])
                    val currentId = ids[position]
                    viewModel.deleteCurImage(currentUri, currentId)
                    imageUris.removeAt(position)
                    ids.removeAt(position)

                    viewPager.adapter?.notifyItemRemoved(position)
                    if (ids.isEmpty()){
                        finish()
                    }
                    true
                }
                else -> false
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        showSystemBars()
    }


    fun setUpPhotoViewTap(viewPager: ViewPager2, position: Int){
        val rv = viewPager.getChildAt(position) as RecyclerView
        val currentHolder = rv.findViewHolderForAdapterPosition(viewPager.currentItem)

        currentHolder?.itemView?.findViewById<PhotoView>(R.id.imageView)?.setOnPhotoTapListener {_, _, _ ->
            if (isAppBarVis) hideAppBar() else showAppBar()
            isAppBarVis = !isAppBarVis
        }
    }

    fun hideAppBar(){
        appBar.animate().apply {
            setDuration(300)
            setInterpolator(DecelerateInterpolator())
            translationY(-appBar.height.toFloat())
            alpha(0f)
        }
        textOfImage.animate().apply {
            setDuration(300)
            setInterpolator(DecelerateInterpolator())
            translationY(-appBar.height.toFloat()-textOfImage.height.toFloat() -50f)
            alpha(0f)
        }
    }

    fun showAppBar(){
        appBar.animate().apply {
            setDuration(300)
            setInterpolator(DecelerateInterpolator())
            translationY(0f)
            alpha(1f)
        }
        textOfImage.animate().apply {
            setDuration(300)
            setInterpolator(DecelerateInterpolator())
            translationY(0f)
            alpha(1f)
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