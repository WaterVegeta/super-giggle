package com.example.tabsgpttutor.homwrklist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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

import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import com.example.tabsgpttutor.data_base.LessonAndTime
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.data_base.Schedule
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.apply
import kotlin.math.ceil


class HomewListFragment : Fragment() {

    private val viewModel: HwViewModel by viewModels()

    private var actionMode: ActionMode? = null
    lateinit var adapter: HwListAdapter
    lateinit var chipBtn: Chip
    lateinit var addFAB: FloatingActionButton

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
    lateinit var listItems: Set<String>
    var allowedDates = mutableSetOf<Long>()
    lateinit var lastValidDate: LocalDate
    lateinit var spinner: Spinner
    val formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd MMM")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_homew_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FragmentCreated", "HwListFragment")
        realm = MyDynamic.realm

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
//        layout.post {
////            layout.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//            layout.translationY = 300f
////            layout.translationX = -110f
//            layout.scaleX = 0.3f
//            layout.scaleY = 0.1f
//            layout.alpha = 0.7f
//            layout.pivotY = layout.height.toFloat()
//            layout.pivotX = layout.width / 2f
//            layout.animate().apply {
//                alpha(1f)
//                scaleX(0.8f)
//                scaleY(0.2f)
//                translationY(0f)
//                translationX(0f)
//                setDuration(150)
//                setInterpolator(AccelerateInterpolator())
//                withEndAction {
//                    layout.animate().apply {
//                        scaleY(1f)
//                        scaleX(1f)
//                        setDuration(300)
//                        setInterpolator(DecelerateInterpolator())
//                    }
//                }
//            }
//
//        }
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
            viewModel.doneHw(clickedLesson)
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeworkList.collect { adapter.updateData(it) }
            }
        }

        addFAB = view.findViewById<FloatingActionButton>(R.id.bottomSheetButton)
        listItems = viewModel.lessons



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
            if (text.isNullOrEmpty()){
                textLayout.error = "Write something"
            }else{
                textLayout.error = null
            }
        }

        chipBtn.setOnClickListener {
            editText.clearFocus()
            findNextNice(LocalDate.now())
            datepickerShow()
        }
        saveBtn.setOnClickListener {
            var noteText = editText.text.toString()

            if (noteText.isEmpty()){
                textLayout.error = "Write something bitch"
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

        val deleteAdapter = DeleteAdapter(emptyList<Homework>())
        lifecycleScope.launch {
            val currentList = viewModel.homeworkList.first()
            val itemsToDelete = currentList.filter { selectedIds.contains(it.id) }
            deleteAdapter.updateData(itemsToDelete)
        }

        rvDelete.layoutManager = LinearLayoutManager(requireContext())
        rvDelete.adapter = deleteAdapter
        tvDelete.text = "Delete ${selectedIds.size} item(s)?"

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
            if (text.isNullOrEmpty()){
                textLayout.error = "Write something"
            }else{
                textLayout.error = null
            }
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
        actionMode?.title = "$count selected"
        actionMode?.menu?.findItem(R.id.action_edit)?.isVisible = (count == 1)
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