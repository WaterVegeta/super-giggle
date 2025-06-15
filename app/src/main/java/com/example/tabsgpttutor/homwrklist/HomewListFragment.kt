package com.example.tabsgpttutor.homwrklist

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.ImageItem
import com.example.tabsgpttutor.data_base.shedule.Schedule
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.apply
import kotlin.math.ceil


class HomewListFragment : Fragment(R.layout.fragment_homew_list) {

    private val viewModel: HwViewModel by viewModels()

    private var actionMode: ActionMode? = null
    lateinit var adapter: HwListAdapter
    lateinit var chipBtn: Chip
    lateinit var addFAB: FloatingActionButton
    val PICK_IMAGE_REQUEST = 1
    val TAKE_PHOTO_REQUEST = 2

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            addFAB.hide()
            mode?.menuInflater?.inflate(R.menu.contextual_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    deleteSelectedItems()
                    mode?.finish()
                    true
                }
                R.id.action_edit -> {
                    editSelectedItem()
                    mode?.finish()
                    true
                }
                R.id.action_share ->{
                    shareItems()
                    mode?.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            addFAB.show()
            Log.d("obDestroy", "destroyed")
            adapter.clearSelection()
            actionMode = null
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var allHomeworks: MutableList<Homework>
    lateinit var realm: Realm
    lateinit var radioGroup: RadioGroup
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    lateinit var listItems: List<String>
    var allowedDates = mutableSetOf<Long>()
    lateinit var lastValidDate: LocalDate
    lateinit var spinner: Spinner
    val formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd MMM")
//    lateinit var galleryLauncher: ActivityResultLauncher<String>
    var selectedHwList: Homework? = null
    lateinit var currentPhotoUri: Uri
    private var currentPhotoPath: String? = null
    var photoUri: Uri? = null

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK){
            result.data?.data?.let { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                saveUri(uri.toString())
            }
        }
//            uri?.let {
//                saveUri(it.toString())
//
//            }
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
            saveUri(uri.toString())

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FragmentCreated", "HwListFragment")
        realm = MyDynamic.realm

//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        intent.type = "image/*"
//        startActivityForResult(intent, 0)


        recyclerView = view.findViewById(R.id.recyclerView)

        val animations = viewModel.getAnimations("Homework")!!
        val layout: CoordinatorLayout = view.findViewById(R.id.main)
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

        adapter = HwListAdapter(object : HwListAdapter.OnItemClickListener{
            override fun onItemLongClick(itemId: String) {
                if (actionMode == null){
                    actionMode = requireActivity().startActionMode(actionModeCallback)
                }
                adapter.toggleSelection(itemId)
                updateTitle()
            }

            override fun onItemClick(itemId: String) {
                if (actionMode != null){
                    adapter.toggleSelection(itemId)
                    updateTitle()
                }
            }
        }, {clickedLesson ->
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

        addFAB = view.findViewById<FloatingActionButton>(R.id.bottomSheetButton)
        lifecycleScope.launch {
            viewModel.uniqueLessons.collect {
                listItems = it
            }

        }



        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = this@HomewListFragment.adapter

        val animBut = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_animation)


        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
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



        addFAB.setOnClickListener {
            bottomSheetShow()
        }
        addFAB.startAnimation(animBut)
        recyclerView.itemAnimator = CustomHwListAnim().apply {
            addDuration = 600
            changeDuration = 300
        }

        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_pop_in)

        recyclerView.startAnimation(anim)


    }
    fun doneHw(clickedLesson: Homework){
        viewModel.doneHw(clickedLesson, null)
        val snackbarText = if(clickedLesson.done) "Marked as undone" else "Done"
        val colorOnSurface = MaterialColors.getColor(addFAB, R.attr.colorOnSurface)
        Snackbar.make(recyclerView, snackbarText, Snackbar.LENGTH_LONG)
            .setBackgroundTint(MaterialColors.getColor(addFAB, com.google.android.material.R.attr.colorSurfaceContainerHigh))
            .setTextColor(colorOnSurface)
            .setActionTextColor(MaterialColors.getColor(addFAB, R.attr.colorOnTertiary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setAction("Undo") {
                viewModel.doneHw(clickedLesson, clickedLesson.done)
            }
            .show()
    }
    fun saveUri(uri: String){

        lifecycleScope.launch {
            realm.write {
                selectedHwList?.let { hw ->
                    findLatest(hw)?.images?.add(
                        ImageItem().apply { imageUri = uri }
                    )

                }
            }
        }
    }

    fun addImage(hwList: Homework){
        val addImageDialog = layoutInflater.inflate(R.layout.bottom_sheet_image, null)
        val pickBtn = addImageDialog.findViewById<MaterialButton>(R.id.pickBtn)
        val cameraBtn = addImageDialog.findViewById<MaterialButton>(R.id.cameraBtn)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(addImageDialog)

        bottomSheetDialog.show()

        pickBtn.setOnClickListener {
//            pickImage(hwList)
            pickPerm(hwList)
        }

        cameraBtn.setOnClickListener {
//            takePicture(hwList)
            cameraPerm(hwList)
        }

    }

    fun cameraPerm(hwList: Homework){
        var perm = mutableListOf<String>()
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) != PERMISSION_GRANTED){
            perm.add(android.Manifest.permission.CAMERA)
        }
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
            perm.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        else if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED &&
            requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED){
                takePicture(hwList)
            }
        if (perm.isNotEmpty()){
            requestPermissions(perm.toTypedArray(), 101)
        }
    }

    fun pickPerm(hwList: Homework){
        var perm = mutableListOf<String>()
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
            perm.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        else pickImage(hwList)
        if (perm.isNotEmpty()){
            requestPermissions( perm.toTypedArray(), 101)
        }
    }
    fun pickImage(hwList: Homework){
        selectedHwList = hwList
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101){
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
                Toast.makeText(requireContext(), "Permissions granted", Toast.LENGTH_SHORT).show()
            }
            else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("Some permissions were denied. The app may not work properly.")
            .setPositiveButton("Go to settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    fun takePicture(hwList: Homework){
//        if (requireContext().checkSelfPermission(READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
//            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), TAKE_PHOTO_REQUEST)
//        }
        selectedHwList = hwList
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if (intent.resolveActivity(requireContext().packageManager) != null){
//            val photoFile = File.createTempFile(
//                "IMG_${System.currentTimeMillis()}",
//                ".jpg",
//                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            )
//            Log.i("path?", "path: ${photoFile.path}, \n ${photoFile.absolutePath} \n ${photoFile.canonicalPath}")
//            photoUri = FileProvider.getUriForFile(
//                requireContext(),
//                "${requireContext().packageName}.fileprovider",
//                photoFile
//            )
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
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
//        currentPhotoPath = photoFile.absolutePath


    }

//    fun checkPermision(): Boolean{
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == ){}
//        return false
//    }

    fun bottomSheetShow(){

//        val bottomSheet = MyBottomSheet()
//        bottomSheet.show(childFragmentManager, bottomSheet.tag)
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

                for (i in 1..7) {
                    val date = LocalDate.now().plusDays(i.toLong())
                    val dayOfWeekName = date.dayOfWeek.name

                    val schedule = realm.query<Schedule>("dayOfWeek == $0", dayOfWeekName).first().find()

                    if (schedule != null) {
                        val isEvenWeek = ceil(date.dayOfYear / 7.0).toInt() % 2 == 0

                        val found = schedule.lessonAndTime.any { lesson ->
                            val subjectFromDb =
                                if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty()) lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd
                            subjectFromDb == selectedLesson
                        }
                        if (found){
                            lastValidDate = date
                            chipBtn.text = lastValidDate.format(formatter)
                            break
                        }
                    }


                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
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
            findNextNice(LocalDate.now())
            datepickerShow()
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
    }


    fun findNextNice(today: LocalDate){
        allowedDates.clear()
        for (i in -30..60) {
            val date = today.plusDays(i.toLong())
            val dayOfWeekName = date.dayOfWeek.name

            val schedule = realm.query<Schedule>("dayOfWeek == $0", dayOfWeekName).first().find()

            if (schedule != null) {
                val isEvenWeek = ceil(date.dayOfYear / 7.0).toInt() % 2 == 0

                val found = schedule.lessonAndTime.any { lesson ->
                    val subjectFromDb =
                        if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty()) lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd
                    subjectFromDb == spinner.selectedItem.toString()
                }
                if (found){

                    val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    allowedDates.add(millis)
                }
            }
        }
    }

    fun datepickerShow(){
        Log.d("allowed DAtes", "dawioi: $allowedDates \nlast valid : $lastValidDate")
        val validator = AllowedDatesValidator(allowedDates)
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
            chipBtn.text = lastValidDate.format(formatter)

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
            viewModel.deleteHw(selectedIds)
            Log.d("DeleteSelected", "deleted and pressed: $selectedIds")
            alertDialog.dismiss()
            actionMode?.finish()
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun createTempUri(ogUri: Uri): Uri{
        return try {
            // Create temp file in app cache
            val cacheFile = File(requireContext().externalCacheDir, "share_${System.currentTimeMillis()}.jpg")

            // Copy content
            requireContext().contentResolver.openInputStream(ogUri)?.use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Get FileProvider URI
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider", // Must match manifest
                cacheFile
            )
        } catch (e: Exception) {
            Log.e("Sharing", "Failed to cache file", e)
            ogUri // Fallback (may still fail)
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
                actionMode?.finish()
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
            actionMode?.finish()
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
        chipBtn.text = lastValidDate.format(formatter)

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
            findNextNice(LocalDate.parse(recivedDate))
            datepickerShow()
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
            actionMode?.title = "$count selected"
            actionMode?.menu?.findItem(R.id.action_edit)?.isVisible = (count == 1)

        } else actionMode?.finish()
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
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