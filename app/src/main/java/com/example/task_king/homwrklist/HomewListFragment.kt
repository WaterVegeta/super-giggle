package com.example.task_king.homwrklist

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup

import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_king.data_base.Homework
import com.example.task_king.HwViewModel
import com.example.task_king.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.apply
import kotlin.math.abs
import androidx.core.graphics.drawable.toDrawable

class HomewListFragment() : Fragment(R.layout.fragment_homew_list) {

    private val viewModel: HwViewModel by viewModels()
    val saveBigModel: SaveBigViewModel by viewModels()


    lateinit var searchToolbar: TextInputLayout
    lateinit var adapter: HwListAdapter
    lateinit var chipBtn: Chip
    lateinit var addFAB: FloatingActionButton
    val PICK_PHOTO = 100
    val TAKE_PHOTO = 101
    lateinit var toolbar: MaterialToolbar
    lateinit var searchText: TextInputEditText
    lateinit var chipGroup : ChipGroup
    lateinit var chipDone: Chip
    lateinit var chipLesson: Chip
    lateinit var chipDate: Chip
    lateinit var chipImage: Chip
    lateinit var chipSort: Chip

    lateinit var chip_layout: CoordinatorLayout
    lateinit var appBar: AppBarLayout

    private lateinit var recyclerView: RecyclerView
    lateinit var listItems: List<String>
    var lastValidDate: LocalDate? = null

    lateinit var spinner: Spinner

    val formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd MMM")
    var selectedHwList: Homework? = null
    private var currentPhotoPath: String? = null

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK){
            result.data?.data?.let { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.saveUri(uri.toString(), selectedHwList, null)
            }
        }
//            uri?.let {
//                saveUri(it.toString())
//
//            }
    }
    val tiramisuLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val context = requireContext()
            val uris = mutableListOf<Uri>()
            val data = result.data

            val clipData = data?.clipData
            val singleUri = data?.data

            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    uris.add(clipData.getItemAt(i).uri)
                }
            } else if (singleUri != null) {
                uris.add(singleUri)
            }

            uris.forEach { uri ->
                val copiedUri = viewModel.copyImageToInternalStorage(context, uri)
                copiedUri?.let {
                    viewModel.saveUri(it[0], selectedHwList, it[1])
                }
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            val file = File(currentPhotoPath!!)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            // Take persistable permission (optional for camera since we control the file)

            // Save the URI string to Realm
            viewModel.saveUri(uri.toString(), selectedHwList, currentPhotoPath)

        }
    }
    lateinit var layout: ConstraintLayout
    var isToolBarShown = false
    lateinit var dateText: String
    lateinit var doneText: String
    lateinit var imageText: String
    lateinit var lessonText: String
    lateinit var botSheet: FrameLayout
    var isBig: Boolean = false

    val ALL = 0
    val TODAY_BEYOND = 1
    val PAST_TODAY = 2

    val DONE = 3
    val NOT_DONE = 4

    val WITH_IMAGE = 5
    val NO_IMAGE = 6

    lateinit var bigEdit: TextInputEditText
    lateinit var guideline: Guideline
    lateinit var divider: View
    lateinit var rightLayout: CoordinatorLayout
    lateinit var leftLayout: ConstraintLayout

    lateinit var shareSheet: FrameLayout


    var isBotSheetActive = false
    var isEditAct = false
    var isOpen = true
    var maxWidth = 0

    var botSheetRatio = 0.7f
    var ratio = 0.7f
    var centerRatio = 0.85f
    var endLine = 0.98f

    var contSpinnerItem = ""
    var contBtsText = ""
    var contBtsDate = ""
    var contState = STATE_HIDDEN
    var isShareActive = false
    var isDefault = true
    var endCorner = 1f
    var startCorner = 0.3f
    lateinit var rect: Rect

    lateinit var shareGroup : RadioGroup
    lateinit var bigBtnSave: Button
    val Int.dpToPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configChip(view)

        checkAnimIntro(view)

        configAppBar(view)

        configToolBar(view)


        adapter = HwListAdapter( object : HwListAdapter.OnItemClickListener{
            override fun onItemLongClick(itemId: String) {
                if (!toolbar.isVisible){
                    showToolBar()
                    Log.i("Toolbar", "long item show toolbar : $itemId")
                }
                Log.i("Toolbar", "long item not show toolbar : $itemId")
                adapter.toggleSelection(itemId)
                updateTitle()
            }

            override fun onItemClick(itemId: String) {
                if (isToolBarShown){
                    adapter.toggleSelection(itemId)
                    updateTitle()
                    Log.i("Toolbar", "click item : $itemId")
                }
            }
        },
            onDone = {clickedLesson ->
                doneHw(clickedLesson)
                     },
            addImage = {hwList ->
                addImage(hwList)
                       },
            startFullScreen = {uris, pos, item, view ->
                startImageFull(uris, pos, item, view)
            })

        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_pop_in)


        var isFabVis = true
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@HomewListFragment.adapter
            itemAnimator = CustomHwListAnim().apply {
                addDuration = 600
                changeDuration = 300
            }
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 10 && isFabVis){
                        isFabVis = false
                        addFAB.animate().apply {
//                            startAnimation(animButHide)
//                            hide()
                            translationY(500f)
                            interpolator = FastOutSlowInInterpolator()
                        }
//                        addFAB.downAnim()
//                        addFAB.hide()
                    }
                    else if (dy < -10 && !isFabVis){
//                        addFAB.backAnim()
                        isFabVis = true
                        addFAB.animate().apply {
//                            startAnimation(animButShow)
//                            show()
                            translationY(0f)
                            interpolator = FastOutSlowInInterpolator()
                        }
                    }
                }
            })
//            addItemDecoration(TopMarginDecoration(this@HomewListFragment.adapter))

            startAnimation(anim)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeworkList.collect { adapter.updateData(it) }

            }
        }

        lifecycleScope.launch {
            viewModel.uniqueLessons.collect {
                listItems = it
            }
        }


        val sWidth = requireContext().resources.configuration.screenWidthDp
        val sHeight = requireContext().resources.configuration.screenHeightDp
        val isLandscape = requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        when {
            sWidth >= 1000 && isLandscape -> {
                isBig = true
                bigMode(view)
            }

            sHeight >= 1000 && !isLandscape -> {
                isBig = false
            }

            sWidth >= 600 && sHeight >= 600 -> {
                isBig = true
                bigMode(view)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (isShareActive){
                        val shareBeh = BottomSheetBehavior.from(shareSheet)
                        when(shareBeh.state){
                            STATE_EXPANDED, STATE_HALF_EXPANDED -> shareBeh.state = STATE_COLLAPSED
                            STATE_COLLAPSED -> shareBeh.state = STATE_HIDDEN
                        }
                    }
                    else if (isEditAct){
                        val botBeh = BottomSheetBehavior.from(botSheet)
                        when(botBeh.state){
                            STATE_EXPANDED, STATE_HALF_EXPANDED -> botBeh.state = STATE_COLLAPSED
                            STATE_COLLAPSED -> botBeh.state = STATE_HIDDEN
                        }
                    }
                    else if (toolbar.isVisible){
                        adapter.clearSelection()
                        updateTitle()
                    }
                    else if (isBotSheetActive){
                        val botBeh = BottomSheetBehavior.from(botSheet)
                        when(botBeh.state){
                            STATE_EXPANDED, STATE_HALF_EXPANDED -> botBeh.state = STATE_COLLAPSED
                            STATE_COLLAPSED -> botBeh.state = STATE_HIDDEN
                        }
                    }
                    else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }

                }
            }
        )

        addFAB.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    rect = Rect()
                    v.getGlobalVisibleRect(rect)
                    v.animate()
                        .setInterpolator(DecelerateInterpolator())
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        // Finger lifted inside the FAB
                        v.animate()
                            .setInterpolator(OvershootInterpolator())
                            .setDuration(140)
                            .scaleX(1f)
                            .scaleY(1f)
                            .withEndAction {
                                if (isBig){
                                    btSheet(view, false)
                                } else bottomSheetShow()
                            }
                            .start()
                    } else {
                        // Finger lifted outside
                        v.animate()
                            .setInterpolator(OvershootInterpolator())
                            .setDuration(140)
                            .scaleX(1f)
                            .scaleY(1f)
                            .start()
                    }

                }
            }
            true
        }
//        addFAB.apply {
//            setOnClickListener {
//                if (isBig){
//                    btSheet(view, false)
//                }
//                else{
//                    bottomSheetShow()
//                }
//            }
//        }
    }

    fun bigMode(view: View){
        bigChip(view)

        botSheet = view.findViewById(R.id.bottomSheet)
        val botSheetBeh = BottomSheetBehavior.from(botSheet)
        botSheetBeh.state = STATE_HIDDEN
        botSheetBeh.setPeekHeight(330, false)

        shareSheet = view.findViewById(R.id.shareBTS)
        val shareBeh = BottomSheetBehavior.from(shareSheet)
        shareBeh.apply {
            state = STATE_HIDDEN
            setPeekHeight(330, false)
        }

        divider = view.findViewById<View>(R.id.divider)
        guideline = view.findViewById<Guideline>(R.id.guideline)


        leftLayout = view.findViewById(R.id.mainLayout)
        rightLayout = view.findViewById(R.id.sideLayout)

        val params = guideline.layoutParams as ConstraintLayout.LayoutParams

        layout.post {
            maxWidth = (layout.width * 0.3f).toInt()
        }

        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener(){
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                val percentage = velocityX / 5000f
                val params = guideline.layoutParams as ConstraintLayout.LayoutParams
                val target = (params.guidePercent + percentage).coerceIn(startCorner, endCorner)
                animateGuide(layout, guideline, target)
                Log.v("FlinG", "x $velocityX , y $velocityY target: $target")

                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                performDoubleTap()

                return super.onDoubleTap(e)
            }
        })

        divider.setOnTouchListener { v, e ->
            gestureDetector.onTouchEvent(e)

            v.performClick()
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val loc = IntArray(2)
                    layout.getLocationOnScreen(loc)
                    val rootX = loc[0]
                    val xInRoot = (e.rawX - rootX).coerceIn(0f, layout.width.toFloat())


                    val percent = (xInRoot / layout.width).coerceIn(startCorner, endCorner)

                    if (isDefault){
                        if (percent > ratio - 0.1f){
                            when{
                                percent >= ratio ->{
                                    clearConst(rightLayout, ConstraintSet.START)

                                }
                                percent < ratio ->{
                                    applyConst(rightLayout, ConstraintSet.START, guideline, ConstraintSet.END)
                                }
                            }
                        }

                    }
                    else{
                        if (percent < ratio + 0.1f){
                            when{
                                percent <= ratio ->{
                                    clearConst(rightLayout, ConstraintSet.END)

                                }
                                percent > ratio ->{
                                    applyConst(rightLayout, ConstraintSet.END, guideline, ConstraintSet.START)
                                }
                            }
                        }
                    }

                    params.guidePercent = percent
                    guideline.layoutParams = params
                    false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    val percent = params.guidePercent
                    if (isDefault){
                        if (isBotSheetActive || isEditAct || isShareActive){
                            if (percent > ratio){
                                animateGuide(layout, guideline, ratio)
                            }
                        }
                        else{
                            if (percent > centerRatio){
                                animateGuide(layout, guideline, endLine)
                                isOpen = false
                            }
                            else if (percent <= centerRatio && percent > ratio){
                                animateGuide(layout, guideline, ratio)
                                isOpen = true
                            }
                            else if (percent <= centerRatio){
                                isOpen = true
                            }
                        }

                    }
                    else{
                        if (isBotSheetActive || isEditAct || isShareActive){
                            if (percent < ratio){
                                animateGuide(layout, guideline, ratio)
                            }
                        }
                        else{
                            if (percent < centerRatio){
                                animateGuide(layout, guideline, endLine)
                                isOpen = false
                            }
                            else if (percent >= centerRatio && percent < ratio){
                                animateGuide(layout, guideline, ratio)
                                isOpen = true
                            }
                            else if (percent >= centerRatio){
                                isOpen = true
                            }
                        }
                    }

                    true
                }
                else -> false
            }
        }
    }

    fun performDoubleTap(){
        isDefault = !isDefault
        if (!isDefault){
            ratio = 0.3f
            botSheetRatio = 0.3f
            centerRatio = 0.15f
            endLine = 0.02f
            startCorner = 0f
            endCorner = 0.7f

            clearConst(leftLayout, ConstraintSet.START)
            clearConst(leftLayout, ConstraintSet.END)
            clearConst(rightLayout, ConstraintSet.END)
            clearConst(rightLayout, ConstraintSet.START)
            applyConst(leftLayout, ConstraintSet.START, guideline, ConstraintSet.START)
            applyConst(leftLayout, ConstraintSet.END, layout, ConstraintSet.END)

            applyConst(rightLayout, ConstraintSet.START, layout, ConstraintSet.START)
            applyConst(rightLayout, ConstraintSet.END, guideline, ConstraintSet.START)

        }
        else{
            ratio = 0.7f
            botSheetRatio = 0.7f
            centerRatio = 0.85f
            endLine = 0.98f
            startCorner = 0.3f
            endCorner = 1f

            clearConst(leftLayout, ConstraintSet.START)
            clearConst(leftLayout, ConstraintSet.END)
            clearConst(rightLayout, ConstraintSet.END)
            clearConst(rightLayout, ConstraintSet.START)

            applyConst(leftLayout, ConstraintSet.START, layout, ConstraintSet.START)
            applyConst(leftLayout, ConstraintSet.END, guideline, ConstraintSet.START)

            applyConst(rightLayout, ConstraintSet.START, guideline, ConstraintSet.END)
            applyConst(rightLayout, ConstraintSet.END, layout, ConstraintSet.END)
        }
        TransitionManager.beginDelayedTransition(layout)
    }

    override fun onStart() {
        super.onStart()
        if (isBig){
            if (saveBigModel.getBTSAct()){
                contSpinnerItem = saveBigModel.getLesson()
                contBtsDate = saveBigModel.getDate()
                contBtsText = saveBigModel.getNote()
                contState = saveBigModel.getState()

                btSheet(requireView(), true)
            }

            if (saveBigModel.getShareAct()){
                bigShare(requireView())
                shareGroup.check(saveBigModel.getShareRadio())
            }
            if (saveBigModel.getEdit()) BottomSheetBehavior.from(botSheet).state = STATE_HIDDEN

            val line = getPrefs().getFloat("line", 0.7f)
            val open = getPrefs().getBoolean("open", true)
            val default = getPrefs().getBoolean("default", true)


            val params = guideline.layoutParams as ConstraintLayout.LayoutParams
            params.guidePercent = line
            guideline.layoutParams = params
            isOpen = open
            isDefault = default
            if (!isDefault){
                isDefault = true
                performDoubleTap()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (saveBigModel.getSelectedMode() && !toolbar.isVisible){
            showToolBar()
            saveBigModel.getSelected().forEach { id ->
                adapter.toggleSelection(id)
                updateTitle()
            }
        }
        if (!isDefault){
            clearConst(leftLayout, ConstraintSet.START)
            clearConst(leftLayout, ConstraintSet.END)
            clearConst(rightLayout, ConstraintSet.END)
            clearConst(rightLayout, ConstraintSet.START)
            applyConst(leftLayout, ConstraintSet.START, guideline, ConstraintSet.START)
            applyConst(leftLayout, ConstraintSet.END, layout, ConstraintSet.END)

            applyConst(rightLayout, ConstraintSet.START, layout, ConstraintSet.START)
            applyConst(rightLayout, ConstraintSet.END, guideline, ConstraintSet.START)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBig){
            val params = guideline.layoutParams as ConstraintLayout.LayoutParams
            getPrefs().edit {
                putBoolean("open", isOpen)
                putFloat("line", params.guidePercent)
                putBoolean("default", isDefault)
            }
        }
    }

    private fun getPrefs() =
        requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override fun onDetach() {
        super.onDetach()

        if (adapter.getSelectedMode()){
            saveBigModel.saveSelected(adapter.getSelectedIds())
            saveBigModel.saveSelectedMode(adapter.getSelectedMode())
        }
        else saveBigModel.saveSelectedMode(adapter.getSelectedMode())

        if (isBig){
            if (isBotSheetActive){
                val state = BottomSheetBehavior.from(botSheet).state
                saveBigModel.save(
                    bigEdit.text.toString(),
                    spinner.selectedItem.toString(),
                    lastValidDate.toString(),
                    state)

                saveBigModel.saveAct(isBotSheetActive)
            }else{
                saveBigModel.saveAct(isBotSheetActive)
            }

            if (isShareActive){
                saveBigModel.saveShare(
                    shareGroup.checkedRadioButtonId,
                )
                saveBigModel.saveShareAct(isShareActive)
            }
            else saveBigModel.saveShareAct(isShareActive)

            saveBigModel.saveEditAct(isEditAct)
        }
    }

    fun showBotOpen(){
        if (!isOpen){
            if (isDefault)applyConst(rightLayout, ConstraintSet.START, guideline, ConstraintSet.END)
            else applyConst(rightLayout, ConstraintSet.END, guideline, ConstraintSet.START)

            animateGuide(layout, guideline, botSheetRatio)
        }
    }

    fun hideBot(){
        if (isDefault) clearConst(rightLayout, ConstraintSet.START)
        else clearConst(rightLayout, ConstraintSet.END)
        animateGuide(layout, guideline, endLine)
    }

    fun clearConst(clear: View, side: Int){
        val layoutParams = rightLayout.layoutParams as ConstraintLayout.LayoutParams
        val set = ConstraintSet()
        set.clone(layout)
        set.clear(clear.id, side)
        set.applyTo(layout)

        layoutParams.matchConstraintMaxWidth = maxWidth
        rightLayout.layoutParams = layoutParams
    }

    fun applyConst(view: View, sideStart: Int, endView: View, sideEnd: Int){
        val layoutParams = rightLayout.layoutParams as ConstraintLayout.LayoutParams
        val set = ConstraintSet()
        set.clone(layout)
        set.connect(view.id, sideStart, endView.id,
            sideEnd)
        set.applyTo(layout)

        layoutParams.matchConstraintMaxWidth = 0
        rightLayout.layoutParams = layoutParams
    }

    fun bigChip(view: View){
        val radioAll = view.findViewById<MaterialRadioButton>(R.id.radioAll)
        val radioDone = view.findViewById<MaterialRadioButton>(R.id.radioDone)
        val radioNotDone = view.findViewById<MaterialRadioButton>(R.id.radioNotDone)
        val groupDone = view.findViewById<RadioGroup>(R.id.groupDone)

        when(viewModel.done_sort.value){
            NOT_DONE -> radioNotDone.isChecked = true
            DONE -> radioDone.isChecked = true
            else -> radioAll.isChecked = true
        }

        groupDone.setOnCheckedChangeListener { v, itemId ->
            when(itemId){
                R.id.radioAll ->{
                    viewModel.changeDone(ALL)
                }
                R.id.radioDone ->{
                    viewModel.changeDone(DONE)
                }
                R.id.radioNotDone ->{
                    viewModel.changeDone(NOT_DONE)
                }
            }
        }

        val dayGroup = view.findViewById<RadioGroup>(R.id.day_group)
        val allDays = view.findViewById<MaterialRadioButton>(R.id.all_days)
        val beyond = view.findViewById<MaterialRadioButton>(R.id.beyond)
        val past = view.findViewById<MaterialRadioButton>(R.id.past)

        when(viewModel.date.value){
            PAST_TODAY -> past.isChecked = true
            TODAY_BEYOND -> beyond.isChecked = true
            else -> allDays.isChecked = true
        }
        dayGroup.setOnCheckedChangeListener { v, itemId ->
            when(itemId){
                allDays.id->{
                    viewModel.changeDate(ALL)
                }
                beyond.id ->{
                    viewModel.changeDate(TODAY_BEYOND)
                }
                past.id ->{
                    viewModel.changeDate(PAST_TODAY)
                }
            }
        }

        val imageGroup : RadioGroup = view.findViewById(R.id.image_group)
        val allImage : MaterialRadioButton = view.findViewById(R.id.all_image)
        val withImage : MaterialRadioButton = view.findViewById(R.id.with_image)
        val withoutImage : MaterialRadioButton = view.findViewById(R.id.without_image)

        when(viewModel.image.value){
            NO_IMAGE -> withoutImage.isChecked = true
            WITH_IMAGE -> withImage.isChecked = true
            else -> allImage.isChecked = true
        }

        imageGroup.setOnCheckedChangeListener { v, itemId ->
            when(itemId){
                allImage.id ->{
                    viewModel.changeImage(ALL)
                }
                withImage.id ->{
                    viewModel.changeImage(WITH_IMAGE)
                }
                withoutImage.id ->{
                    viewModel.changeImage(NO_IMAGE)
                }
            }
        }

        val accendingRadio : MaterialRadioButton = view.findViewById(R.id.accending)
        val decendRadio : MaterialRadioButton = view.findViewById(R.id.decending)

        val radioSortDate = view.findViewById<MaterialRadioButton>(R.id.radioSortDate)
        val radioSortHomework = view.findViewById<MaterialRadioButton>(R.id.radioSortHomework)
        val radioSortLesson = view.findViewById<MaterialRadioButton>(R.id.radioSortLesson)

        when(viewModel.sortOrder.value){
            Sort.ASCENDING -> accendingRadio.isChecked = true
            Sort.DESCENDING -> decendRadio.isChecked = true
        }
        for (i in listOf(decendRadio, accendingRadio)){
            i.setOnCheckedChangeListener {v, state ->
                if (state){
                    val sortOrder = when(v.text.toString()){
                        getString(R.string.descending) -> Sort.DESCENDING
                        else -> Sort.ASCENDING
                    }
                    viewModel.changeSortOrder(sortOrder)
                }
            }
        }
        when(viewModel.sortField.value){
            "lesson" -> radioSortLesson.isChecked = true
            "note" -> radioSortHomework.isChecked = true
            else -> radioSortDate.isChecked = true
        }
        val radioList = listOf(radioSortDate, radioSortLesson, radioSortHomework)
        for (i in radioList){
            i.setOnCheckedChangeListener { v, state ->
                if (state){
                    val sortField = when(v.text.toString()){
                        getString(R.string.homework_sort) -> "note"
                        getString(R.string.lesson_sort) -> "lesson"
                        else -> "date"
                    }
                    viewModel.changeSortField(sortField)
                }
            }
        }

        val lessonGroup : RadioGroup = view.findViewById(R.id.lesson_group)
        val allLesson : MaterialRadioButton = view.findViewById(R.id.all_lessons)

        if (viewModel.lesson.value == radioAll.text.toString()){
            allLesson.isChecked = true
        }

        allLesson.setOnCheckedChangeListener { _, state ->
            if (state){
                viewModel.changeLesson("All")
            }
        }

        layout.post {
            for (i in listItems){
                val radioButton = MaterialRadioButton(requireContext())
                radioButton.text = i.toString()
                radioButton.layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                radioButton.updatePadding(
                    left = 10.dpToPx,
                    right = 10.dpToPx
                )
                radioButton.textSize = 24f
                if (viewModel.lesson.value == i.toString()){
                    radioButton.isChecked = true
                }
                radioButton.setOnCheckedChangeListener {button, state ->
                    if (state){
                        viewModel.changeLesson(i)
                    }
                }
                lessonGroup.addView(radioButton)
            }

        }

    }

    fun startImageFull(uris: Array<String>, pos: Int, item: Homework, view: View){
        val intent = Intent(requireContext(), FullScreenImage::class.java)
        intent.putExtra("imageUris", uris)
        intent.putExtra("startPosition", pos)
        intent.putExtra("homework", item.note)
        intent.putExtra("lesson", item.lesson)
        intent.putExtra("ids", item.images.map { it.id }.toTypedArray())
        val options = ActivityOptions.makeSceneTransitionAnimation(
            requireActivity(), view, "image"
        )

        requireContext().startActivity(intent, options.toBundle())
    }

    fun animateGuide(constraintLayout: ConstraintLayout,
                     guideline: Guideline,
                     to: Float,
                     actionStart: () -> Unit = {},
                     onEnd: () -> Unit = {}) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.setGuidelinePercent(guideline.id, to)

        TransitionManager.beginDelayedTransition(
            constraintLayout,
            ChangeBounds().apply {
                duration = 500
                interpolator = OvershootInterpolator()
                addListener(object : Transition.TransitionListener{
                    override fun onTransitionStart(p0: Transition?) {
                        actionStart()
                    }

                    override fun onTransitionEnd(p0: Transition?) {
                        onEnd()
                    }

                    override fun onTransitionCancel(p0: Transition?) {
                    }

                    override fun onTransitionPause(p0: Transition?) {
                    }

                    override fun onTransitionResume(p0: Transition?) {
                    }

                })
            }
        )
        constraintSet.applyTo(constraintLayout)
    }

    fun bigShare(view: View){
        val beh = BottomSheetBehavior.from(shareSheet)
        isShareActive = true
        if(isBotSheetActive || isEditAct){
            BottomSheetBehavior.from(botSheet).state = STATE_HIDDEN
        }
        showBotOpen()
        beh.state = STATE_EXPANDED

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    STATE_HIDDEN -> {
                        isShareActive = false
                        saveBigModel.saveShareAct(isShareActive)
                        if (!isOpen && !isEditAct){
                            hideBot()
                        }
                    }
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do something for slide offset.
            }
        }

        beh.addBottomSheetCallback(bottomSheetCallback)

        val closeBtn = view.findViewById<ImageButton>(R.id.closeBtnShare)
        shareGroup = view.findViewById<RadioGroup>(R.id.radio_group)
        val shareBtn = view.findViewById<MaterialButton>(R.id.share_button)


        closeBtn.setOnClickListener {
            beh.state = STATE_HIDDEN
        }

        shareBtn.setOnClickListener {
            val selected = adapter.getSelectedIds()
            lifecycleScope.launch {
                val lista = viewModel.homeworkList.first()
                val shareList = lista.filter { selected.contains(it.id) }
                val textShare : MutableList<String> = mutableListOf()
                val images = arrayListOf<Uri>()
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyy")


                when(shareGroup.checkedRadioButtonId){
                    R.id.share_all ->{
                        for (i in shareList){
                            val date = LocalDate.parse(i.date).format(formatter)
                            textShare.add(getString(R.string.lesson) + ": " + i.lesson + "\n" + getString(R.string.homework) + ": " + i.note + getString(R.string.due_date) + ": " + date)
                            i.images.map { it.imageUri.toUri() }.forEach {
                                images.add(it)
                            }
                        }

                        if (images.isEmpty()){
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textShare.joinToString(separator = "\n\n"))
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share text"))

                        }
                        else{
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND_MULTIPLE
                                type = "image/*"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, images)
                                putExtra(Intent.EXTRA_TEXT, textShare.joinToString(separator = "\n\n"))
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                            }
                            val chooserIntent = Intent.createChooser(shareIntent, "Share Images").apply {
                                // Add flags to chooser intent too
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            val resInfoList = requireContext().packageManager
                                .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)

                            for (info in resInfoList) {
                                for (uri in images) {
                                    requireContext().grantUriPermission(
                                        info.activityInfo.packageName,
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                            }
                            startActivity(chooserIntent)
                        }

                    }
                    R.id.share_text ->{
                        for (i in shareList){
                            val date = LocalDate.parse(i.date).format(formatter)
                            textShare.add(getString(R.string.lesson) + ": " + i.lesson + "\n" + getString(R.string.homework) + ": " + i.note + getString(R.string.due_date) + ": " + date)
                        }

                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, textShare.joinToString(separator = "\n\n"))
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share text"))
                    }
                    R.id.share_photo ->{
                        for (i in shareList){
                            i.images.map { it.imageUri.toUri() }.forEach {
                                images.add(it)
                            }
                        }
                        if (images.isEmpty()){
                            Toast.makeText(requireContext(), getString(R.string.no_images_to_share), Toast.LENGTH_LONG).show()
                        }
                        else{
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND_MULTIPLE
                                type = "image/*"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, images)
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                            }
                            val chooserIntent = Intent.createChooser(shareIntent, "Share Images").apply {
                                // Add flags to chooser intent too
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            val resInfoList = requireContext().packageManager
                                .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)

                            for (info in resInfoList) {
                                for (uri in images) {
                                    requireContext().grantUriPermission(
                                        info.activityInfo.packageName,
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                            }
                            startActivity(chooserIntent)
                        }

                    }
                }
                withContext(Dispatchers.Main) {
                    hideActionMode()
                }
            }
            beh.state = STATE_HIDDEN
        }
    }

    fun bigBotEdit(view: View, item: Homework){
        lastValidDate = LocalDate.parse(item.date)
        val behavior = BottomSheetBehavior.from(botSheet)
        showBotOpen()
        if (isShareActive) BottomSheetBehavior.from(shareSheet).state = STATE_HIDDEN else if (isBotSheetActive) behavior.state = STATE_HIDDEN

        isEditAct = true


        spinner = view.findViewById<Spinner>(R.id.lessonSpinner)
        chipBtn = view.findViewById<Chip>(R.id.dateChip)
        val textLayout = view.findViewById<TextInputLayout>(R.id.textLayout)
        val editText = view.findViewById<TextInputEditText>(R.id.titleEditText)
        bigBtnSave = view.findViewById<Button>(R.id.saveBtn)
        val closeBtn = view.findViewById<ImageButton>(R.id.closeBtn)
        val tvHw = view.findViewById<TextView>(R.id.tvHomework)

        tvHw.text = getString(R.string.edit_homework)

        val btsCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    STATE_HIDDEN -> {
                        editText.setText("")
                        isEditAct = false
                        if (!isOpen && !isShareActive){
                            hideBot()
                        }
//                        bImm.hideSoftInputFromWindow(bigEdit.windowToken, 0)
                    }
                    BottomSheetBehavior.STATE_DRAGGING ->{
                        editText.clearFocus()
//                        bImm.hideSoftInputFromWindow(bigEdit.windowToken, 0)
                    }
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do something for slide offset.
            }
        }
        behavior.addBottomSheetCallback(btsCallback)

        val spinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listItems.toList())
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLesson = parent.getItemAtPosition(position).toString()

                lastValidDate = if(selectedLesson == item.lesson) LocalDate.parse(item.date)
                else viewModel.findValidDate(selectedLesson)
                chipBtn.text = lastValidDate?.format(formatter)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
//        chipBtn.text = lastValidDate?.format(formatter)

        editText.requestFocus()
        editText.doOnTextChanged { text, start, before, count ->
            textLayout.error = null
            textLayout.isErrorEnabled = false
        }


        val lessonIndex = listItems.indexOf(item.lesson)
        if (lessonIndex != -1) spinner.setSelection(lessonIndex)
        editText.setText(item.note)

        behavior.state = STATE_EXPANDED

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                bigBtnSave.performClick()
                true
            } else false
        }

        chipBtn.setOnClickListener {
            datePickerShow(LocalDate.parse(item.date), spinner.selectedItem.toString())
        }

        closeBtn.setOnClickListener {
            behavior.state = STATE_HIDDEN
        }

        bigBtnSave.setOnClickListener {
            var noteText = editText.text.toString()
            if (noteText.isBlank()){
                textLayout.error = getString(R.string.write_homework)
                textLayout.isErrorEnabled = true
                return@setOnClickListener
            }

            lifecycleScope.launch {
                viewModel.editHw(item.date, item.note, item.lesson, lastValidDate.toString(),
                    noteText, spinner.selectedItem.toString())
                withContext(Dispatchers.Main) {
                    behavior.state = STATE_HIDDEN
                }
            }
        }
    }

    fun btSheet(view: View, modeCont: Boolean){
        val behavior = BottomSheetBehavior.from(botSheet)

        isBotSheetActive = true

        showBotOpen()

        spinner = view.findViewById<Spinner>(R.id.lessonSpinner)
        chipBtn = view.findViewById<Chip>(R.id.dateChip)
        val textLayout = view.findViewById<TextInputLayout>(R.id.textLayout)
        bigEdit = view.findViewById<TextInputEditText>(R.id.titleEditText)
        bigBtnSave = view.findViewById<Button>(R.id.saveBtn)
        val closeBtn = view.findViewById<ImageButton>(R.id.closeBtn)
        val tvHw = view.findViewById<TextView>(R.id.tvHomework)

        tvHw.text = getString(R.string.add_homework)
        closeBtn.setOnClickListener {
            behavior.state = STATE_HIDDEN
        }

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listItems.toList())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        if (modeCont){
            behavior.state = if (contState == BottomSheetBehavior.STATE_DRAGGING || contState == BottomSheetBehavior.STATE_SETTLING)
                STATE_HALF_EXPANDED else contState
            val pos = listItems.indexOf(contSpinnerItem)
            if (pos != -1) spinner.setSelection(pos, true)
            bigEdit.setText(contBtsText)
            lastValidDate = LocalDate.parse(contBtsDate)
            chipBtn.text = lastValidDate?.format(formatter)
        }
        else{
            bigEdit.setText("")
            behavior.state = STATE_EXPANDED
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLesson = parent.getItemAtPosition(position).toString()
                lastValidDate = viewModel.findValidDate(selectedLesson)
                chipBtn.text = lastValidDate?.format(formatter)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        bigEdit.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && behavior.state == STATE_COLLAPSED){
                behavior.state = STATE_EXPANDED
            }
        }

        val bImm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        bigEdit.postDelayed({
            bigEdit.requestFocus()

            bImm.showSoftInput(bigEdit, InputMethodManager.SHOW_IMPLICIT)
            }, 400)


        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    STATE_HIDDEN -> {
                        bigEdit.setText("")
                        isBotSheetActive = false
                        if (!isOpen && !isShareActive){
                            hideBot()
                        }
                        bigEdit.clearFocus()
                        bImm.hideSoftInputFromWindow(bigEdit.windowToken, 0)
                    }
                    BottomSheetBehavior.STATE_DRAGGING ->{
                        bigEdit.clearFocus()
                        bImm.hideSoftInputFromWindow(bigEdit.windowToken, 0)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do something for slide offset.
            }
        }

        behavior.addBottomSheetCallback(bottomSheetCallback)

        bigEdit.doOnTextChanged { text, start, before, count ->
            textLayout.isErrorEnabled = false
            textLayout.error = null
        }

        bigEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                bigBtnSave.performClick()
                true
            } else false
        }

        chipBtn.setOnClickListener {
            if (spinner.selectedItem == null)
                return@setOnClickListener
            bigEdit.clearFocus()
            datePickerShow(LocalDate.now(), spinner.selectedItem.toString())
        }

        bigBtnSave.setOnClickListener {
            var noteText = bigEdit.text.toString()

            if (noteText.isBlank()){
                textLayout.error = getString(R.string.write_homework)
                textLayout.isErrorEnabled = true
                return@setOnClickListener
            }
            if (spinner.selectedItem == null){
                behavior.state = STATE_HIDDEN
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val list = viewModel.homeworkList.first()
                val position = list.find { it.date == lastValidDate.toString() && it.lesson == spinner.selectedItem.toString() }

                if (position != null){
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.this_homework_already_exists),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    viewModel.addHw(lastValidDate.toString(), noteText, spinner.selectedItem.toString())
                    withContext(Dispatchers.Main) {
                        bigEdit.clearFocus()
                        bImm.hideSoftInputFromWindow(bigEdit.windowToken, 0)
                        behavior.state = STATE_HIDDEN
                    }
                }
            }

        }
    }

    fun configAppBar(view: View){
        searchText = view.findViewById(R.id.searchText)
        searchText.doOnTextChanged {text, _, _, _ ->
            viewModel.onSearch(text.toString())
        }
        var expanded = false
        appBar.setExpanded(false, false)

        val animDuration = 200L
        val animInter = DecelerateInterpolator()
        appBar.addOnOffsetChangedListener { v, verticalOffset ->
            val totalScrollRange = appBar.totalScrollRange

            if (verticalOffset == 0) {
                // Fully expanded
                expanded = true
                recyclerView.animate().apply {
                    translationY(appBar.height.toFloat())
                    interpolator = animInter
                    duration = animDuration
                    withEndAction {
//                        recyclerView.translationY = 0f
                        recyclerView.setPadding(
                            recyclerView.paddingLeft,
                            recyclerView.paddingTop,
                            recyclerView.paddingRight,
                            appBar.height
                        )
                    }
                    start()
                }
            }
            else if (abs(verticalOffset) >= totalScrollRange) {
                // Fully collapsed
                expanded = false
                recyclerView.setPadding(
                    recyclerView.paddingLeft,
                    recyclerView.paddingTop,
                    recyclerView.paddingRight,
                    0
                )
                recyclerView.animate().apply {
                    translationY(0f)
                    interpolator = animInter
                    duration = animDuration
                    start()
                }
            }
        }

        searchToolbar.setStartIconOnClickListener {
            if (!isBig){
                chip_layout.isVisible = true
                appBar.setExpanded(!expanded, true)
            }

        }
    }

    fun configToolBar(view: View){
        toolbar = view.findViewById<MaterialToolbar>(R.id.actionToolBar)
        toolbar.apply {
            navigationIcon?.mutate()?.setTint(MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorOnSurface))
            menu.findItem(R.id.action_delete).icon?.mutate()?.setTint(MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorError))
            menu.findItem(R.id.action_share).icon?.mutate()?.setTint(MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorSecondary))
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete -> {
                        deleteSelectedItems()
                        true
                    }

                    R.id.action_edit -> {
                        editSelectedItem()
                        true
                    }

                    R.id.action_share -> {
                        if (isBig){
                            bigShare(view)
                        }
                        else shareItems()
                        true
                    }

                    else -> false
                }
            }
            setNavigationOnClickListener {
                hideActionMode()
            }
        }
    }

    fun checkAnimIntro(view: View){
        val animations = viewModel.getAnimations("Homework")!!
        layout = view.findViewById(R.id.main)
        if (animations.firstAnim){
            val interp = when(animations.firstInterpolator){
                "AccelerateInterpolator" -> AccelerateInterpolator()
                "AccelerateDecelerateInterpolator" -> AccelerateDecelerateInterpolator()
                "AnticipateInterpolator" -> AnticipateInterpolator()
                "AnticipateOvershootInterpolator" -> AnticipateOvershootInterpolator()
                "BounceInterpolator" -> BounceInterpolator()
                "DecelerateInterpolator" -> DecelerateInterpolator()
                "FastOutSlowInInterpolator" -> FastOutSlowInInterpolator()
                "FastOutLinearInInterpolator" -> FastOutLinearInInterpolator()
                "LinearOutSlowInInterpolator" -> LinearOutSlowInInterpolator()
                "LinearInterpolator" -> LinearInterpolator()
                "OvershootInterpolator" -> OvershootInterpolator()
                else -> LinearInterpolator()
            }

            layout.post {
                layout.scaleX = animations.firstScaleX
                layout.scaleY = animations.firstScaleY
                layout.alpha = animations.firstAlpha
                layout.pivotY = layout.height.toFloat() * animations.pivotY
                layout.pivotX = layout.width * animations.pivotX
                layout.translationX = animations.firstTranslationX
                layout.translationY = animations.firstTranslationY
                layout.animate().apply {
                    if (animations.secondAnim){
                        alpha(animations.secondAlpha)
                        scaleX(animations.secondScaleX)
                        scaleY(animations.secondScaleY)
                        translationX(animations.secondTranslationX)
                        translationY(animations.secondTranslationY)
                        duration = animations.firstDuration
                        interpolator = interp
                        withEndAction {
                            val interp2 = when(animations.secondInterpolator){
                                "AccelerateInterpolator" -> AccelerateInterpolator()
                                "AccelerateDecelerateInterpolator" -> AccelerateDecelerateInterpolator()
                                "AnticipateInterpolator" -> AnticipateInterpolator()
                                "AnticipateOvershootInterpolator" -> AnticipateOvershootInterpolator()
                                "BounceInterpolator" -> BounceInterpolator()
                                "DecelerateInterpolator" -> DecelerateInterpolator()
                                "FastOutSlowInInterpolator" -> FastOutSlowInInterpolator()
                                "FastOutLinearInInterpolator" -> FastOutLinearInInterpolator()
                                "LinearOutSlowInInterpolator" -> LinearOutSlowInInterpolator()
                                "LinearInterpolator" -> LinearInterpolator()
                                "OvershootInterpolator" -> OvershootInterpolator()
                                else -> LinearInterpolator()
                            }
                            layout.animate().apply {
                                alpha(1f)
                                scaleX(1f)
                                scaleY(1f)
                                translationX(0f)
                                translationY(0f)
                                duration = animations.secondDuration
                                interpolator = interp2
                            }
                        }

                    } else{
                        alpha(1f)
                        scaleX(1f)
                        scaleY(1f)
                        translationX(0f)
                        translationY(0f)
                        duration = animations.firstDuration
                        interpolator = interp

                    }
                }

            }

        }
    }

    fun configChip(view: View){
        dateText = getString(R.string.chip_date)
        doneText = getString(R.string.chip_done)
        imageText = getString(R.string.chip_image)
        lessonText = getString(R.string.chip_lesson)

        recyclerView = view.findViewById(R.id.recyclerView)
        addFAB = view.findViewById<FloatingActionButton>(R.id.bottomSheetButton)
        searchToolbar = view.findViewById(R.id.searchToolbar)

        chip_layout = view.findViewById(R.id.chip_layout)
        appBar = view.findViewById(R.id.app_bar)



        chipGroup = view.findViewById(R.id.chipGroup)
        chipDone = view.findViewById(R.id.chipDone)
        chipLesson = view.findViewById(R.id.chipLesson)
        chipDate = view.findViewById(R.id.chipDate)
        chipImage = view.findViewById(R.id.chipImage)
        chipSort = view.findViewById(R.id.chipSort)
        val sortChips = listOf(chipDone, chipLesson, chipDate, chipImage, chipSort)
        for (i in sortChips){
            when(i){
                chipDone -> {
                    if (viewModel.done_sort.value != ALL){
                        val text = if (viewModel.done_sort.value == DONE){
                            getString(R.string.done)
                        } else getString(R.string.not_done)
                        checkChip(i, true, text)
                    }
                }
                chipDate -> {
                    if (viewModel.date.value != TODAY_BEYOND){
                        val text = when(viewModel.date.value){
                            PAST_TODAY-> getString(R.string.from_past_to_today)
                            ALL -> getString(R.string.all_days)
                            else -> getString(R.string.from_today_and_beyond)
                        }
                        checkChip(i, true, text)

                    }
                }
                chipImage -> {
                    if (viewModel.image.value != ALL){
                        val text = if (viewModel.image.value == WITH_IMAGE){
                            getString(R.string.with_image)
                        }
                        else getString(R.string.without_image)

                        checkChip(i, true, text)
                    }
                }
                chipLesson -> {
                    if (viewModel.lesson.value != "All"){
                        checkChip(i, true, viewModel.lesson.value)
                    }
                }
                chipSort ->{
                    if (viewModel.sortField.value != "date"){
                        val text = when(viewModel.sortField.value){
                            "note" -> getString(R.string.homework_sort)
                            else ->  getString(R.string.lesson_sort)
                        }
                        checkChip(i, true, text)
                    }
                }
            }
            i.setOnClickListener {
                showChipBottom(i.id)
            }
        }
    }

    fun checkChip(chip: Chip, check: Boolean, newText: String){
        chip.text = newText
//        if (check){
//            chip.apply {
//                text = newText
////                isCheckable = true
////                isChecked = true
//            }
//        }
//        else {
//            chip.apply {
//                text = newText
////                isChecked = false
////                isCheckable = false
//            }
//        }
    }

    @SuppressLint("InflateParams")
    fun showChipBottom(chipId: Int){
        val dialog = layoutInflater.inflate(R.layout.chip_bottom_sheet, null)
        val radioAll = dialog.findViewById<MaterialRadioButton>(R.id.radioAll)
        val radioDone = dialog.findViewById<MaterialRadioButton>(R.id.radioDone)
        val radioNotDone = dialog.findViewById<MaterialRadioButton>(R.id.radioNotDone)
        val groupDone = dialog.findViewById<RadioGroup>(R.id.groupDone)
        val closeBtn = dialog.findViewById<ImageButton>(R.id.closeBtn)
        val tvChoose = dialog.findViewById<TextView>(R.id.tvChoose)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialog)

        closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        when(chipId){
            R.id.chipDate ->{
                tvChoose.text = dateText
                radioAll.text = getString(R.string.all_days)
                radioDone.text = getString(R.string.from_today_and_beyond)
                radioNotDone.text = getString(R.string.from_past_to_today)

                when(viewModel.date.value){
                    PAST_TODAY -> radioNotDone.isChecked = true
                    TODAY_BEYOND -> radioDone.isChecked = true
                    else -> radioAll.isChecked = true
                }
                groupDone.setOnCheckedChangeListener { v, itemId ->
                    when(itemId){
                        R.id.radioAll ->{
                            viewModel.changeDate(ALL)
                            checkChip(chipDate, false, dateText)
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioDone ->{
                            checkChip(chipDate, true, radioDone.text.toString())
                            viewModel.changeDate(TODAY_BEYOND)
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioNotDone ->{
                            checkChip(chipDate, true, radioNotDone.text.toString())
                            viewModel.changeDate(PAST_TODAY)
                            bottomSheetDialog.dismiss()
                        }
                    }
                }
            }
            R.id.chipDone ->{

                when(viewModel.done_sort.value){
                    NOT_DONE -> radioNotDone.isChecked = true
                    DONE -> radioDone.isChecked = true
                    else -> radioAll.isChecked = true
                }
                tvChoose.text = doneText
                groupDone.setOnCheckedChangeListener { v, itemId ->
                    when(itemId){
                        R.id.radioAll ->{
                            viewModel.changeDone(ALL)
                            checkChip(chipDone, false, doneText)
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioDone ->{
                            checkChip(chipDone, true, radioDone.text.toString())
                            viewModel.changeDone(DONE)
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioNotDone ->{
                            checkChip(chipDone, true, radioNotDone.text.toString())
                            viewModel.changeDone(NOT_DONE)
                            bottomSheetDialog.dismiss()
                        }
                    }
                }
            }
            R.id.chipImage ->{
                tvChoose.text = imageText
                radioDone.text = getString(R.string.with_image)
                radioNotDone.text = getString(R.string.without_image)
                when(viewModel.image.value){
                    NO_IMAGE -> radioNotDone.isChecked = true
                    WITH_IMAGE -> radioDone.isChecked = true
                    else -> radioAll.isChecked = true
                }

                groupDone.setOnCheckedChangeListener { v, itemId ->
                    when(itemId){
                        R.id.radioAll ->{
                            checkChip(chipImage, false, imageText)

                            viewModel.changeImage(ALL)
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioDone ->{
                            checkChip(chipImage, true, radioDone.text.toString())

                            viewModel.changeImage(WITH_IMAGE)
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioNotDone ->{
                            checkChip(chipImage, true, radioNotDone.text.toString())

                            viewModel.changeImage(NO_IMAGE)
                            bottomSheetDialog.dismiss()
                        }
                    }
                }
            }
            R.id.chipLesson ->{
                tvChoose.text = lessonText
                radioDone.isVisible = false
                radioNotDone.isVisible = false

                if (viewModel.lesson.value == radioAll.text.toString()){
                    radioAll.isChecked = true
                }
                radioAll.setOnCheckedChangeListener { _, state ->
                    if (state){
                        checkChip(chipLesson, false, lessonText)
                        viewModel.changeLesson("All")
                        bottomSheetDialog.dismiss()
                    }
                }
                for (i in listItems){
                    val radioButton = MaterialRadioButton(requireContext())
                    radioButton.text = i.toString()
                    radioButton.layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    )
                    radioButton.updatePadding(
                        left = 10.dpToPx,
                        right = 10.dpToPx
                    )

                    radioButton.textSize = 24f
                    if (viewModel.lesson.value == i.toString()){
                        radioButton.isChecked = true
                    }
                    radioButton.setOnCheckedChangeListener {button, state ->
                        if (state){
                            checkChip(chipLesson, true, i)
                            viewModel.changeLesson(i)
                            bottomSheetDialog.dismiss()
                        }
                    }
                    groupDone.addView(radioButton)
                }
            }
            R.id.chipSort ->{
                val linearLayout = dialog.findViewById<LinearLayout>(R.id.sortLayout)
                val radioSortDate = dialog.findViewById<MaterialRadioButton>(R.id.radioSortDate)
                val radioSortHomework = dialog.findViewById<MaterialRadioButton>(R.id.radioSortHomework)
                val radioSortLesson = dialog.findViewById<MaterialRadioButton>(R.id.radioSortLesson)

                tvChoose.text = getString(R.string.sort_order)
                linearLayout.isVisible = true
                radioAll.text = getString(R.string.ascending)
                radioDone.text = getString(R.string.descending)
                radioNotDone.isVisible = false
                when(viewModel.sortOrder.value){
                    Sort.ASCENDING -> radioAll.isChecked = true
                    Sort.DESCENDING -> radioDone.isChecked = true
                }
                for (i in listOf(radioDone, radioAll)){
                    i.setOnCheckedChangeListener {v, state ->
                        if (state){
                            val sortOrder = when(v.text.toString()){
                                getString(R.string.descending) -> Sort.DESCENDING
                                else -> Sort.ASCENDING
                            }
                            viewModel.changeSortOrder(sortOrder)
                        }
                    }
                }
                when(viewModel.sortField.value){
                    "lesson" -> radioSortLesson.isChecked = true
                    "note" -> radioSortHomework.isChecked = true
                    else -> radioSortDate.isChecked = true
                }
                val radioList = listOf(radioSortDate, radioSortLesson, radioSortHomework)
                for (i in radioList){
                    i.setOnCheckedChangeListener { v, state ->
                        if (state){
                            val sortField = when(v.text.toString()){
                                getString(R.string.homework_sort) -> "note"
                                getString(R.string.lesson_sort) -> "lesson"
                                else -> "date"
                            }
                            viewModel.changeSortField(sortField)
                        }
                    }
                }
            }
        }
        bottomSheetDialog.behavior.state = STATE_EXPANDED
        bottomSheetDialog.show()
    }

    fun showToolBar(){
        isToolBarShown = true
        toolbar.isVisible = true
        toolbar.translationX = recyclerView.width.toFloat()
        searchToolbar.animate().apply {
            translationX(-recyclerView.width.toFloat())
            duration = 200
            interpolator = DecelerateInterpolator()

        }

        appBar.animate().apply {
            translationX(-recyclerView.width.toFloat())
            duration = 200
            interpolator = DecelerateInterpolator()
        }
        toolbar.animate().apply {
            translationX(0f)
            duration = 200
            interpolator = DecelerateInterpolator()
            withEndAction {
                Log.i("Toolbar", "show toolbar")
//                chipGroup.isVisible = false

                searchToolbar.visibility = View.INVISIBLE }

        }

        addFAB.animate().apply {
            translationY(500f)
        }
        if (isBotSheetActive) BottomSheetBehavior.from(botSheet).state = STATE_HIDDEN
    }

    fun hideActionMode(){

        searchToolbar.isVisible = true
        isToolBarShown = false

        toolbar.animate().apply {
            translationX(recyclerView.width.toFloat())
            duration = 200
            interpolator = DecelerateInterpolator()

        }
        searchToolbar.animate().apply {
            searchToolbar.translationX = -recyclerView.width.toFloat()
            translationX(0f)
            duration = 200
            interpolator = DecelerateInterpolator()
            withEndAction {
                Log.i("Toolbar", "hide tool bar")
                toolbar.isVisible = false
            }
        }
        appBar.animate().apply {
            appBar.translationX = -recyclerView.width.toFloat()
            translationX(0f)
            duration = 200
            interpolator = DecelerateInterpolator()
        }
        addFAB.animate().apply {
            translationY(0f)
        }

        if (isShareActive) BottomSheetBehavior.from(shareSheet).state = STATE_HIDDEN
        else if (isEditAct) BottomSheetBehavior.from(botSheet).state = STATE_HIDDEN

        adapter.clearSelection()

    }

    fun doneHw(clickedLesson: Homework){
        viewModel.doneHw(clickedLesson, null)
        val snackbarText = if(clickedLesson.done) getString(R.string.marked_as_undone) else getString(R.string.done)
        val colorOnSurface = MaterialColors.getColor(addFAB, R.attr.colorOnSurface)

        Snackbar.make(recyclerView, snackbarText, Snackbar.LENGTH_LONG)
            .setAnchorView(addFAB)
            .setBackgroundTint(MaterialColors.getColor(addFAB, com.google.android.material.R.attr.colorSurfaceContainerHigh))
            .setTextColor(colorOnSurface)
            .setActionTextColor(MaterialColors.getColor(addFAB, R.attr.colorTertiary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setAction(getString(R.string.undo)) {
                viewModel.doneHw(clickedLesson, clickedLesson.done)
            }
            .show()
    }

    @SuppressLint("InflateParams")
    fun addImage(hwList: Homework){
        val addImageDialog = layoutInflater.inflate(R.layout.bottom_sheet_image, null)
        val pickBtn = addImageDialog.findViewById<MaterialButton>(R.id.pickBtn)
        val cameraBtn = addImageDialog.findViewById<MaterialButton>(R.id.cameraBtn)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(addImageDialog)

        bottomSheetDialog.behavior.state = STATE_EXPANDED
        bottomSheetDialog.show()

        pickBtn.setOnClickListener {
            pickPerm()
            selectedHwList = hwList
            bottomSheetDialog.dismiss()
        }

        cameraBtn.setOnClickListener {
            cameraPerm()
            selectedHwList = hwList
            bottomSheetDialog.dismiss()
        }

    }

    fun cameraPerm(){
        var perm = mutableListOf<String>()
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED){
            perm.add(Manifest.permission.CAMERA)
            requestPermissions(perm.toTypedArray(), TAKE_PHOTO)
        }
        else if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED){
                takePicture()
            }
    }

    fun pickPerm(){
        var perm = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (requireContext().checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PERMISSION_GRANTED){
                perm.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else pickImage(true)
        }
        else{
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
                perm.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else pickImage(false)

        }
        if (perm.isNotEmpty()){
            requestPermissions( perm.toTypedArray(), PICK_PHOTO)
        }
    }

    fun pickImage(tiramisu: Boolean){
        if (tiramisu){
            val pickImages = Intent(MediaStore.ACTION_PICK_IMAGES)
            pickImages.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 69)
            tiramisuLauncher.launch(pickImages)
        }
        else{
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            galleryLauncher.launch(intent)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        if (requestCode == PICK_PHOTO){
            if (grantResults.all { it == PERMISSION_GRANTED }){
                pickImage(isTiramisu)
            }
            else {
                showPermissionDeniedDialog("photo")
            }
        }
        else if (requestCode == TAKE_PHOTO){
            if (grantResults.all { it == PERMISSION_GRANTED }){
                takePicture()
            }
            else {
                showPermissionDeniedDialog("camera")
            }
        }
    }

    private fun showPermissionDeniedDialog(action: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_denied))
            .setMessage(
                getString(
                    R.string.permission_for_were_denied_the_app_may_not_work_properly,
                    action
                ))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    fun takePicture(){
        val photoFile = File.createTempFile(
            "JPEG_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        ).apply { currentPhotoPath = absolutePath }

        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(photoUri)
    }

    @SuppressLint("InflateParams")
    fun bottomSheetShow(){
        val dialogView = layoutInflater.inflate(R.layout.hw_list_sheet, null)
        spinner = dialogView.findViewById<Spinner>(R.id.lessonSpinner)
        chipBtn = dialogView.findViewById<Chip>(R.id.dateChip)
        val textLayout = dialogView.findViewById<TextInputLayout>(R.id.textLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveBtn)
        val closeBtn = dialogView.findViewById<ImageButton>(R.id.closeBtn)


        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listItems.toList())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLesson = parent.getItemAtPosition(position).toString()
                lastValidDate = viewModel.findValidDate(selectedLesson)
                chipBtn.text = lastValidDate?.format(formatter)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)

        closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setOnShowListener {
            editText.postDelayed({
                editText.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 400)
        }

        editText.doOnTextChanged { text, start, before, count ->
            textLayout.isErrorEnabled = false
            textLayout.error = null
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                saveBtn.performClick()
                true
            } else false
        }

        chipBtn.setOnClickListener {
            if (spinner.selectedItem == null)
                return@setOnClickListener
            editText.clearFocus()
            datePickerShow(LocalDate.now(), spinner.selectedItem.toString())
        }
        saveBtn.setOnClickListener {
            var noteText = editText.text.toString()

            if (noteText.isBlank()){
                textLayout.error = getString(R.string.write_homework)
                textLayout.isErrorEnabled = true
                return@setOnClickListener
            }
            if (spinner.selectedItem == null){
                bottomSheetDialog.dismiss()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val list = viewModel.homeworkList.first()
                val position = list.find { it.date == lastValidDate.toString() && it.lesson == spinner.selectedItem.toString() }

                if (position != null){
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.this_homework_already_exists),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else {
                        viewModel.addHw(lastValidDate.toString(), noteText, spinner.selectedItem.toString())
                        withContext(Dispatchers.Main) {
                            editText.clearFocus()
                            bottomSheetDialog.hide()
                        }
                    }
            }

        }
        bottomSheetDialog.behavior.state = STATE_EXPANDED
        bottomSheetDialog.show()
    }

    fun datePickerShow(date: LocalDate, selected: String){
        val validator = AllowedDatesValidator(viewModel.findNextNice(date, selected))
        val constraints = CalendarConstraints.Builder()
            .setValidator(validator)
            .build()

        val select = lastValidDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraints)
            .setSelection(select)
            .build()

        picker.show(parentFragmentManager, "date_picker")

        picker.addOnPositiveButtonClickListener { millis ->
            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            lastValidDate = selectedDate
            chipBtn.text = lastValidDate?.format(formatter)

        }
    }

    private fun deleteSelectedItems() {
        val selectedIds = adapter.getSelectedIds()
        if (selectedIds.isEmpty()) return
        Log.d("DeleteSelected", "trying to delete: $selectedIds")

        val dialogView = layoutInflater.inflate(R.layout.confirm_delete_dialog, null)
        val rvDelete = dialogView.findViewById<RecyclerView>(R.id.rvDelete)
        val tvDelete = dialogView.findViewById<TextView>(R.id.tvDelete)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val deleteAdapter = DeleteAdapter()
        tvDelete.text = getString(R.string.delete_item_s, selectedIds.size.toString())
        rvDelete.apply {
            adapter = deleteAdapter
            lifecycleScope.launch {
                viewModel.homeworkList.collect {
                    val currentList = it
                    val itemsToDelete = currentList.filter { selectedIds.contains(it.id) }
                    deleteAdapter.submitList(itemsToDelete)
                }
            }
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()

        btnDelete.setOnClickListener {
            viewModel.deleteHw(selectedIds, requireContext())
            Log.d("DeleteSelected", "deleted and pressed: $selectedIds")
            alertDialog.dismiss()
            hideActionMode()
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun shareItems() {
        val dialogView = layoutInflater.inflate(R.layout.share_options, null)
        val closeBtn = dialogView.findViewById<ImageButton>(R.id.closeBtnShare)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group)
        val shareBtn = dialogView.findViewById<MaterialButton>(R.id.share_button)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)

        closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        shareBtn.setOnClickListener {
            val selected = adapter.getSelectedIds()
            lifecycleScope.launch {
                val lista = viewModel.homeworkList.first()
                val shareList = lista.filter { selected.contains(it.id) }
                val textShare : MutableList<String> = mutableListOf()
                val images = arrayListOf<Uri>()
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyy")

                when(radioGroup.checkedRadioButtonId){
                    R.id.share_all ->{
                        for (i in shareList){
                            val date = LocalDate.parse(i.date).format(formatter)
                            textShare.add(getString(R.string.lesson) + ": " + i.lesson + "\n" + getString(R.string.homework) + ": " + i.note + getString(R.string.due_date) + ": " + date)
                            i.images.map { it.imageUri.toUri() }.forEach {
                                images.add(it)
                            }
                        }

                        if (images.isEmpty()){
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textShare.joinToString(separator = "\n\n"))
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share text"))

                        }
                        else{
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND_MULTIPLE
                                type = "image/*"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, images)
                                putExtra(Intent.EXTRA_TEXT, textShare.joinToString(separator = "\n\n"))
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                            }
                            val chooserIntent = Intent.createChooser(shareIntent, "Share Images").apply {
                                // Add flags to chooser intent too
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            val resInfoList = requireContext().packageManager
                                .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)

                            for (info in resInfoList) {
                                for (uri in images) {
                                    requireContext().grantUriPermission(
                                        info.activityInfo.packageName,
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                            }
                            startActivity(chooserIntent)
                        }

                    }
                    R.id.share_text ->{
                        for (i in shareList){
                            val date = LocalDate.parse(i.date).format(formatter)
                            textShare.add(getString(R.string.lesson) + ": " + i.lesson + "\n" + getString(R.string.homework) + ": " + i.note + getString(R.string.due_date) + ": " + date)
                        }
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, textShare.joinToString(separator = "\n\n"))
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share text"))
                    }
                    R.id.share_photo ->{
                        for (i in shareList){
                            i.images.map { it.imageUri.toUri() }.forEach {
                                images.add(it)
                            }
                        }
                        if (images.isEmpty()){
                            Toast.makeText(requireContext(),
                                getString(R.string.no_images_to_share), Toast.LENGTH_LONG).show()
                        }
                        else{
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND_MULTIPLE
                                type = "image/*"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, images)
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                            }
                            val chooserIntent = Intent.createChooser(shareIntent, "Share Images").apply {
                                // Add flags to chooser intent too
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            val resInfoList = requireContext().packageManager
                                .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)

                            for (info in resInfoList) {
                                for (uri in images) {
                                    requireContext().grantUriPermission(
                                        info.activityInfo.packageName,
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                            }
                            startActivity(chooserIntent)
                        }

                    }
                }
                withContext(Dispatchers.Main) {
                    hideActionMode()
                }
            }
            bottomSheetDialog.dismiss()

        }

        bottomSheetDialog.behavior.state = STATE_EXPANDED
        bottomSheetDialog.show()
    }

    private fun editSelectedItem() {
        val selected = adapter.getSelectedIds().firstOrNull()
        if (selected != null) {
//            val itemToEdit = allHomeworks[selected[0]]
            lifecycleScope.launch {
                Log.d("selected", "id: $selected")
                val list = viewModel.findHwById(selected.toString())
                if (list != null){
                    if (isBig) bigBotEdit(requireView(), list) else editBottomSheet(list.date, list.note, list.lesson)
                    Log.d("editTextwda", "recieved: $list")

                }
            }
            // open edit screen or dialog
//            hideActionMode()
        }
    }

    @SuppressLint("InflateParams")
    fun editBottomSheet(recivedDate: String, recivedNote: String, recivedLesson: String){
        lastValidDate = LocalDate.parse(recivedDate)
        val dialogView = layoutInflater.inflate(R.layout.hw_list_sheet, null)
        spinner = dialogView.findViewById<Spinner>(R.id.lessonSpinner)
        chipBtn = dialogView.findViewById<Chip>(R.id.dateChip)
        val textLayout = dialogView.findViewById<TextInputLayout>(R.id.textLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveBtn)
        val closeBtn = dialogView.findViewById<ImageButton>(R.id.closeBtn)
        val tvHomework = dialogView.findViewById<TextView>(R.id.tvHomework)

        tvHomework.text = getString(R.string.edit_homework)

        val spinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listItems.toList())
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinAdapter
        chipBtn.text = lastValidDate?.format(formatter)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)
        editText.requestFocus()
        editText.doOnTextChanged { text, start, before, count ->
            textLayout.error = null
            textLayout.isErrorEnabled = false
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLesson = parent.getItemAtPosition(position).toString()

                lastValidDate = if(selectedLesson == recivedLesson) LocalDate.parse(recivedDate)
                else viewModel.findValidDate(selectedLesson)
                chipBtn.text = lastValidDate?.format(formatter)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }


        val lessonIndex = listItems.indexOf(recivedLesson)
        if (lessonIndex != -1) spinner.setSelection(lessonIndex)
        editText.setText(recivedNote)

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                saveBtn.performClick()
                true
            } else false
        }

        chipBtn.setOnClickListener {
            datePickerShow(LocalDate.parse(recivedDate), spinner.selectedItem.toString())
        }

        closeBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        saveBtn.setOnClickListener {
            var noteText = editText.text.toString()
            if (noteText.isBlank()){
                textLayout.error = getString(R.string.write_homework)
                textLayout.isErrorEnabled = true
                return@setOnClickListener
            }

            lifecycleScope.launch {
                viewModel.editHw(recivedDate, recivedNote, recivedLesson, lastValidDate.toString(),
                    noteText, spinner.selectedItem.toString())
                    withContext(Dispatchers.Main) {
                        bottomSheetDialog.dismiss()
                    }
            }

        }

        bottomSheetDialog.behavior.state = STATE_EXPANDED
        bottomSheetDialog.show()


    }

    private fun updateTitle() {
        val count = adapter.getSelectedIds().size
        if (count != 0){
            toolbar.title = getString(R.string.selected, count.toString())
            toolbar.menu?.findItem(R.id.action_edit)?.isVisible = (count == 1)

        } else {
            hideActionMode()
        }
    }
}