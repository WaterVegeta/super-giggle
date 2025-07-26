package com.example.task_king.homwrklist

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.ActionMode
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
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup

import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
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

import eightbitlab.com.blurview.BlurView
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.apply

class HomewListFragment : Fragment(R.layout.fragment_homew_list) {

    private val viewModel: HwViewModel by viewModels()


    lateinit var searchToolbar: TextInputLayout
    private var actionMode: ActionMode? = null
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
    lateinit var chip_blur: BlurView
    lateinit var appBar: AppBarLayout

    lateinit var blurView: BlurView

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
    lateinit var layout: CoordinatorLayout
    var isToolBarShown = false
    lateinit var dateText: String
    lateinit var doneText: String
    lateinit var imageText: String
    lateinit var lessonText: String

    val ALL = 0
    val TODAY_BEYOND = 1
    val PAST_TODAY = 2

    val DONE = 3
    val NOT_DONE = 4

    val WITH_IMAGE = 5
    val NO_IMAGE = 6

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FragmentCreated", "HwListFragment")

        dateText = getString(R.string.chip_date)
        doneText = getString(R.string.chip_done)
        imageText = getString(R.string.chip_image)
        lessonText = getString(R.string.chip_lesson)

        recyclerView = view.findViewById(R.id.recyclerView)
        addFAB = view.findViewById<FloatingActionButton>(R.id.bottomSheetButton)
        searchToolbar = view.findViewById(R.id.searchToolbar)
//        blurView = view.findViewById(R.id.blurView)
        chip_blur = view.findViewById(R.id.chip_blur)

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

        val animations = viewModel.getAnimations("Homework")!!
        layout= view.findViewById(R.id.main)
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
                        setDuration(animations.firstDuration)
                        setInterpolator(interp)
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
                                setDuration(animations.secondDuration)
                                setInterpolator(interp2)
                            }
                        }

                    } else{
                        alpha(1f)
                        scaleX(1f)
                        scaleY(1f)
                        translationX(0f)
                        translationY(0f)
                        setDuration(animations.firstDuration)
                        setInterpolator(interp)

                    }
                }

            }

        }
        searchText = view.findViewById(R.id.searchText)
        searchText.doOnTextChanged {text, _, _, _ ->
            viewModel.onSearch(text.toString())
        }
        var expanded = false

        val animDuration = 200L
        val animInter = DecelerateInterpolator()
        appBar.addOnOffsetChangedListener { v, verticalOffset ->
            val totalScrollRange = appBar.totalScrollRange

            if (verticalOffset == 0) {
                // Fully expanded
                expanded = true
                recyclerView.animate().apply {
                    translationY(appBar.height.toFloat())
                    setInterpolator(animInter)
                    setDuration(animDuration)
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
            } else if (Math.abs(verticalOffset) >= totalScrollRange) {
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
                    setInterpolator(animInter)
                    setDuration(animDuration)
                    start()
                }
            }
        }

        searchToolbar.setStartIconOnClickListener {

            chip_layout.isVisible = true
            appBar.setExpanded(!expanded, true)

        }
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
                        shareItems()
                        true
                    }

                    else -> false
                }
            }
            setNavigationOnClickListener {
                hideActionMode()
            }
        }

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
        }, onDone = {clickedLesson ->
            doneHw(clickedLesson)
        },
            addImage = {hwList ->
                addImage(hwList)
            })

        val animButShow = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_animation_show)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_pop_in)
        val animButHide = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_anim_hide)

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
                            setInterpolator(FastOutSlowInInterpolator())
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
                            setInterpolator(FastOutSlowInInterpolator())
                        }
                    }
                }
            })
//            addItemDecoration(TopMarginDecoration(this@HomewListFragment.adapter))

            startAnimation(anim)
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

//            val layoutMarg = toolbar.layoutParams as ViewGroup.MarginLayoutParams
//            layoutMarg.topMargin = systemBars.top
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
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

        addFAB.apply {
            setOnClickListener {
                bottomSheetShow()
            }
//            startAnimation(animBut)
        }
    }

    fun checkChip(chip: Chip, check: Boolean, newText: String){
        if (check){
            chip.apply {
                text = newText
                isCheckable = true
                isChecked = true
            }
        }
        else {
            chip.apply {
                text = newText
                isChecked = false
                isCheckable = false
            }
        }
    }


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
                bottomSheetDialog.show()
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
                bottomSheetDialog.show()


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
                bottomSheetDialog.show()
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
                    radioButton.textSize = 28f
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
                bottomSheetDialog.show()
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
                bottomSheetDialog.show()
            }
        }

    }
    fun showToolBar(){
        isToolBarShown = true
        toolbar.isVisible = true
        toolbar.translationX = recyclerView.width.toFloat()
        searchToolbar.animate().apply {
            translationX(-recyclerView.width.toFloat())
            setDuration(200)
            setInterpolator(DecelerateInterpolator())

        }

        appBar.animate().apply {
            translationX(-recyclerView.width.toFloat())
            setDuration(200)
            setInterpolator(DecelerateInterpolator())
        }
        toolbar.animate().apply {
            translationX(0f)
            setDuration(200)
            setInterpolator(DecelerateInterpolator())
            withEndAction {
                Log.i("Toolbar", "show toolbar")
//                chipGroup.isVisible = false

                searchToolbar.visibility = View.INVISIBLE }

        }

        addFAB.animate().apply {
            translationY(500f)
        }
    }
    fun hideActionMode(){
        searchToolbar.isVisible = true
        isToolBarShown = false

        toolbar.animate().apply {
            translationX(recyclerView.width.toFloat())
            setDuration(200)
            setInterpolator(DecelerateInterpolator())

        }
        searchToolbar.animate().apply {
            searchToolbar.translationX = -recyclerView.width.toFloat()
            translationX(0f)
            setDuration(200)
            setInterpolator(DecelerateInterpolator())
            withEndAction {
                Log.i("Toolbar", "hide tool bar")
                toolbar.isVisible = false }
            addFAB.animate().apply {
                translationY(0f)
            }
        }
        appBar.animate().apply {
            appBar.translationX = -recyclerView.width.toFloat()
            translationX(0f)
            setDuration(200)
            setInterpolator(DecelerateInterpolator())
        }

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

    fun addImage(hwList: Homework){
        val addImageDialog = layoutInflater.inflate(R.layout.bottom_sheet_image, null)
        val pickBtn = addImageDialog.findViewById<MaterialButton>(R.id.pickBtn)
        val cameraBtn = addImageDialog.findViewById<MaterialButton>(R.id.cameraBtn)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(addImageDialog)

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
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
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

    fun bottomSheetShow(){
        val dialogView = layoutInflater.inflate(R.layout.hw_list_sheet, null)
        spinner = dialogView.findViewById<Spinner>(R.id.lessonSpinner)
        chipBtn = dialogView.findViewById<Chip>(R.id.dateChip)
        val textLayout = dialogView.findViewById<TextInputLayout>(R.id.textLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val saveBtn = dialogView.findViewById<Button>(R.id.btnClose)


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
                TODO()
            }
        }

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)

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
            datepickerShow(LocalDate.now(), spinner.selectedItem.toString())
        }
        saveBtn.setOnClickListener {
            var noteText = editText.text.toString()

            if (noteText.isEmpty()){
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
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        viewModel.addHw(lastValidDate.toString(), noteText, spinner.selectedItem.toString())
                    }
                    withContext(Dispatchers.Main) {
                        editText.clearFocus()
                        bottomSheetDialog.hide()

                    }
            }

        }
        bottomSheetDialog.show()
        dialogView.post {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    it.requestLayout()
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    fun datepickerShow(date: LocalDate, selected: String){
        val validator = AllowedDatesValidator(viewModel.findNextNice(date, selected))
        val constraints = CalendarConstraints.Builder()
            .setValidator(validator)
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraints)
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

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

    private fun shareItems() {
        val dialogView = layoutInflater.inflate(R.layout.share_options, null)
        val closeBtn = dialogView.findViewById<ImageButton>(R.id.closeBtn)
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

                when(radioGroup.checkedRadioButtonId){
                    R.id.share_all ->{
                        for (i in shareList){
                            textShare.add(
                                getString(R.string.lesson) + ": " + i.lesson + "\n " + getString(R.string.homework) +
                                    ": " + i.note + "\n" + getString(R.string.due_date) + ": " + i.date)
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
                            textShare.add(getString(R.string.lesson_sort) + ": " + i.lesson + "\n " + getString(R.string.homework_sort) +
                                    ": " + i.note + "\n" + getString(R.string.due_date) + ": " + i.date)
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
                            Toast.makeText(requireContext(), "no images to share", Toast.LENGTH_LONG).show()
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

        bottomSheetDialog.show()
    }

    private fun editSelectedItem() {
        val selected = adapter.getSelectedIds().firstOrNull()
        if (selected != null) {
//            val itemToEdit = allHomeworks[selected[0]]
            lifecycleScope.launch {
                Log.d("selected", "id: ${selected}")
                val list = viewModel.findHwById(selected.toString())
                if (list != null){
                    editBottomSheet(list.date, list.note, list.lesson)
                    Log.d("editTextwda", "recieved: $list")

                }
            }
            // open edit screen or dialog
            hideActionMode()
        }
    }

    fun editBottomSheet(recivedDate: String, recivedNote: String, recivedLesson: String){
        lastValidDate = LocalDate.parse(recivedDate)
        val dialogView = layoutInflater.inflate(R.layout.hw_list_sheet, null)
        spinner = dialogView.findViewById<Spinner>(R.id.lessonSpinner)
        chipBtn = dialogView.findViewById<Chip>(R.id.dateChip)
        val textLayout = dialogView.findViewById<TextInputLayout>(R.id.textLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val saveBtn = dialogView.findViewById<Button>(R.id.btnClose)

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


        val lessonIndex = listItems.indexOf(recivedLesson)
        if (lessonIndex != -1) spinner.setSelection(lessonIndex)
        editText.setText(recivedNote)

        chipBtn.setOnClickListener {
            datepickerShow(LocalDate.parse(recivedDate), spinner.selectedItem.toString())
        }


        saveBtn.setOnClickListener {
            var noteText = editText.text.toString()
            if (noteText.isEmpty()){
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

    override fun onPause() {
        super.onPause()
//        hideActionMode()
        Log.d("FragmentPaused", "HwListFragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FragmentDestroyed", "HwListFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("FragmentDestroyedView", "HwListFragment")
    }


}