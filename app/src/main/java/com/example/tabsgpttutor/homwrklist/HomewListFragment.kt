package com.example.tabsgpttutor.homwrklist

import android.Manifest
import android.app.Activity
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup

import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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
import com.example.tabsgpttutor.data_base.Homework
import com.example.tabsgpttutor.HwViewModel
import com.example.tabsgpttutor.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import com.google.android.material.sidesheet.SideSheetDialog
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
import java.time.format.DateTimeFormatter
import kotlin.apply


class HomewListFragment : Fragment(R.layout.fragment_homew_list) {

    private val viewModel: HwViewModel by viewModels()

    lateinit var searchToolbar: TextInputLayout
    lateinit var sortFAB: FloatingActionButton
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FragmentCreated", "HwListFragment")

        recyclerView = view.findViewById(R.id.recyclerView)
        addFAB = view.findViewById<FloatingActionButton>(R.id.bottomSheetButton)
        sortFAB = view.findViewById(R.id.sortFAB)
        searchToolbar = view.findViewById(R.id.searchToolbar)

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
                    if (viewModel.done.value != "All"){
                        i.text = viewModel.done.value
                        i.isCheckable = true
                        i.isChecked = true
                    }
                }
                chipDate -> {
                    if (viewModel.date.value != "date >= $0"){
                        i.apply {
                            text = when(viewModel.date.value){
                                "date <= $0" -> "From past to today"
                                null -> "All days"
                                else -> "From today and beyond"
                            }
                            isChecked = true
                            isCheckable = true
                        }
                    }
                }
                chipImage -> {
                    if (viewModel.image.value != "All"){
                        i.apply {
                            text = viewModel.image.value
                            isChecked = true
                            isCheckable = true
                        }
                    }
                }
                chipLesson -> {
                    if (viewModel.lesson.value != "All"){
                        i.apply {
                            text = viewModel.lesson.value
                            isChecked = true
                            isCheckable = true
                        }
                    }
                }
                chipSort ->{
                    if (viewModel.sortField.value != "date"){
                        i.apply {
                            text = when(viewModel.sortField.value){
                                "note" -> "Homework"
                                else ->  "Lesson"
                            }
                            isChecked = true
                            isCheckable = true
                        }
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

        searchToolbar.setStartIconOnClickListener {
            chipGroup.isVisible = !chipGroup.isVisible
        }
        toolbar = view.findViewById<MaterialToolbar>(R.id.actionToolBar)
        toolbar.navigationIcon?.mutate()?.setTint(MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorOnSurface))
        toolbar.menu.findItem(R.id.action_delete).icon?.mutate()?.setTint(MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorError))
        toolbar.menu.findItem(R.id.action_share).icon?.mutate()?.setTint(MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorSecondary))
        toolbar.setOnMenuItemClickListener {
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
        toolbar.setNavigationOnClickListener {
            hideActionMode()
        }
        adapter = HwListAdapter(object : HwListAdapter.OnItemClickListener{
            override fun onItemLongClick(itemId: String) {
                if (!toolbar.isVisible){
//                    actionMode = requireActivity().startActionMode(actionModeCallback)
                    showToolBar()
                }
                adapter.toggleSelection(itemId)
                updateTitle()
            }

            override fun onItemClick(itemId: String) {
                if (toolbar.isVisible){
                    adapter.toggleSelection(itemId)
                    updateTitle()
                }
            }
        }, onDone = {clickedLesson ->
            doneHw(clickedLesson)
        },
            addImage = {hwList ->
                addImage(hwList)
            })

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
        val animBut = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_animation)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_pop_in)

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
                    if (dy>0){
                        addFAB.hide()
                    }
                    else if (dy<0 && addFAB.isShown == false){
                        addFAB.apply {
                            startAnimation(animBut)
                            show()
                        }
                    }
                }
            })
            startAnimation(anim)
        }

        addFAB.apply {
            setOnClickListener {
                bottomSheetShow()
            }
            startAnimation(animBut)
        }
        sortFAB.setOnClickListener {
            sortView()
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
                tvChoose.text = "Date"
                radioAll.text = "All days"
                radioDone.text = "From today and beyond"
                radioNotDone.text = "From past to today"

                when(viewModel.date.value){
                    "date <= $0" -> radioNotDone.isChecked = true
                    "date >= $0" -> radioDone.isChecked = true
                    else -> radioAll.isChecked = true
                }
                groupDone.setOnCheckedChangeListener { v, itemId ->
                    when(itemId){
                        R.id.radioAll ->{
                            viewModel.changeDate(radioAll.text.toString())
                            chipDate.apply {
                                text = "date"
                                isCheckable = false
                                isChecked = false
                            }
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioDone ->{
                            chipDate.apply {
                                text = radioDone.text
                                isCheckable = true
                                isChecked = true
                            }
                            viewModel.changeDate(radioDone.text.toString())
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioNotDone ->{
                            chipDate.apply {
                                text = radioNotDone.text
                                isCheckable = true
                                isChecked = true
                            }
                            viewModel.changeDate(radioNotDone.text.toString())
                            bottomSheetDialog.dismiss()
                        }
                    }
                }
                bottomSheetDialog.show()
            }
            R.id.chipDone ->{

                when(viewModel.done.value){
                    radioNotDone.text.toString() -> radioNotDone.isChecked = true
                    radioDone.text.toString() -> radioDone.isChecked = true
                    else -> radioAll.isChecked = true
                }
                tvChoose.text = "Done"
                groupDone.setOnCheckedChangeListener { v, itemId ->
                    when(itemId){
                        R.id.radioAll ->{
                            viewModel.changeDone(radioAll.text.toString())
                            chipDone.apply {
                                text = "done"
                                isCheckable = false
                                isChecked = false
                            }
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioDone ->{
                            chipDone.apply {
                                text = radioDone.text
                                isCheckable = true
                                isChecked = true
                            }
                            viewModel.changeDone(radioDone.text.toString())
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioNotDone ->{
                            chipDone.apply {
                                text = radioNotDone.text
                                isCheckable = true
                                isChecked = true
                            }
                            viewModel.changeDone(radioNotDone.text.toString())
                            bottomSheetDialog.dismiss()
                        }
                    }
                }
                bottomSheetDialog.show()


            }
            R.id.chipImage ->{
                tvChoose.text = "Image"
                radioDone.text = "With image"
                radioNotDone.text = "Without image"
                when(viewModel.image.value){
                    radioNotDone.text.toString() -> radioNotDone.isChecked = true
                    radioDone.text.toString() -> radioDone.isChecked = true
                    else -> radioAll.isChecked = true
                }

                groupDone.setOnCheckedChangeListener { v, itemId ->
                    when(itemId){
                        R.id.radioAll ->{
                            viewModel.changeImage(radioAll.text.toString())
                            chipImage.text = "image"
                            chipImage.isCheckable = false
                            chipImage.isChecked = false
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioDone ->{
                            chipImage.text = "With image"
                            chipImage.isCheckable = true
                            chipImage.isChecked = true
                            viewModel.changeImage(radioDone.text.toString())
                            bottomSheetDialog.dismiss()
                        }
                        R.id.radioNotDone ->{
                            chipImage.text = "Without image"
                            chipImage.isCheckable = true
                            chipImage.isChecked = true
                            viewModel.changeImage(radioNotDone.text.toString())
                            bottomSheetDialog.dismiss()
                        }
                    }
                }
                bottomSheetDialog.show()
            }
            R.id.chipLesson ->{
                tvChoose.text = "Lesson"
                radioDone.isVisible = false
                radioNotDone.isVisible = false

                if (viewModel.lesson.value == radioAll.text.toString()){
                    radioAll.isChecked = true
                }
                radioAll.setOnCheckedChangeListener { _, state ->
                    if (state){
                        chipLesson.text = "lesson"
                        chipLesson.isCheckable = false
                        chipLesson.isChecked = false
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
                            chipLesson.text = i
                            viewModel.changeLesson(i)
                            chipLesson.isCheckable = true
                            chipLesson.isChecked = true
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

                tvChoose.text = "Sort order"
                linearLayout.isVisible = true
                radioAll.text = "Ascending"
                radioDone.text = "Descending"
                radioNotDone.isVisible = false
                when(viewModel.sortOrder.value){
                    Sort.ASCENDING -> radioAll.isChecked = true
                    Sort.DESCENDING -> radioDone.isChecked = true
                }
                for (i in listOf(radioDone, radioAll)){
                    i.setOnCheckedChangeListener {v, state ->
                        if (state){
                            val sortOrder = when(v.text.toString()){
                                "Descending" -> Sort.DESCENDING
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
                                "Homework" -> "note"
                                "Lesson" -> "lesson"
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
        toolbar.isVisible = true
        toolbar.translationX = recyclerView.width.toFloat()
        searchToolbar.animate().apply {
            translationX(-recyclerView.width.toFloat())
            setDuration(200)
            setInterpolator(DecelerateInterpolator())

        }
        toolbar.animate().apply {
            translationX(0f)
            setDuration(200)
            setInterpolator(DecelerateInterpolator())
            withEndAction {
                chipGroup.isVisible = false
                searchToolbar.visibility = View.INVISIBLE }

        }

        addFAB.animate().apply {
            translationY(500f)
        }
    }
    fun hideActionMode(){
        searchToolbar.isVisible = true


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
            withEndAction { toolbar.isVisible = false }
            addFAB.animate().apply {
                translationY(0f)
            }
        }

        adapter.clearSelection()
//        actionMode?.finish()
//        searchToolbar.postDelayed({
//            searchToolbar.isGone = false
//        }, 470)
//        actionMode?.hide(2000)
//        actionMode?.finish()
    }


    fun sortView(){
        val sideView = layoutInflater.inflate(R.layout.side_sheet_layout, null)
        val sideSheet = SideSheetDialog(requireContext())
        sideSheet.setContentView(sideView)
        sideSheet.show()
    }

    fun doneHw(clickedLesson: Homework){
        viewModel.doneHw(clickedLesson, null)
        val snackbarText = if(clickedLesson.done) "Marked as undone" else "Done"
        val colorOnSurface = MaterialColors.getColor(addFAB, R.attr.colorOnSurface)
        val botNav = requireActivity().findViewById<BottomNavigationView>(R.id.navBar)
        Snackbar.make(recyclerView, snackbarText, Snackbar.LENGTH_LONG)
            .setAnchorView(addFAB)
            .setBackgroundTint(MaterialColors.getColor(addFAB, com.google.android.material.R.attr.colorSurfaceContainerHigh))
            .setTextColor(colorOnSurface)
            .setActionTextColor(MaterialColors.getColor(addFAB, R.attr.colorTertiary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setAction("Undo") {
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
            .setTitle("Permission Denied")
            .setMessage("Permission for ${action} were denied. The app may not work properly.")
            .setPositiveButton("Go to settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
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
        editText.requestFocus()

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


        editText.doOnTextChanged { text, start, before, count ->
            textLayout.isErrorEnabled = false
            textLayout.error = null
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
                textLayout.error = "Write something bitch"
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
                            "This hw already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        viewModel.addHw(lastValidDate.toString(), noteText, spinner.selectedItem.toString())
                    }
                    withContext(Dispatchers.Main) {
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
        tvDelete.text = "Delete ${selectedIds.size} item(s)?"
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
        val selected = adapter.getSelectedIds()
        lifecycleScope.launch {
            val lista = viewModel.homeworkList.first()
            val shareList = lista.filter { selected.contains(it.id) }
            val textShare : MutableList<String> = mutableListOf()
            val images = arrayListOf<Uri>()
            for (i in shareList){
                textShare.add("Lesson: " + i.lesson + "\nHomework: " + i.note + "\nDue date: " + i.date)
                i.images.map { it.imageUri.toUri() }.forEach {
                    images.add(it)
                }
            }

            if (images.isEmpty()){
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
//                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, images)
                    putExtra(Intent.EXTRA_TEXT, textShare.joinToString(separator = "\n\n"))
//                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
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
            withContext(Dispatchers.Main) {
                hideActionMode()
            }
        }
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
                textLayout.error = "Write homework bitch"
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
            toolbar.title = "$count selected"
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