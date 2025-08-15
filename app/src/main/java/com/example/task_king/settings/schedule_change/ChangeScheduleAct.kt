package com.example.task_king.settings.schedule_change

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.task_king.R
import com.example.task_king.SettingsViewModel
import com.example.task_king.data_base.LessonChange
import com.example.task_king.data_base.TimeChange
import com.example.task_king.data_base.temp_schedule.TempLessonAndTime
import com.example.task_king.data_base.temp_schedule.TempSchedule
import com.example.task_king.settings.schedule_change.adapters.AddLessonScheduleAdapter
import com.example.task_king.settings.schedule_change.adapters.ChooseTimeAdapter
import com.example.task_king.settings.schedule_change.adapters.SchedulePagerAdapter
import com.example.task_king.widget.DynamicWidProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.LocalTime

class ChangeScheduleAct : AppCompatActivity() {
    private lateinit var collapsingToolbar: CollapsingToolbarLayout

    private val viewModel: SettingsViewModel by viewModels()
    lateinit var viewPager: ViewPager2
    lateinit var alertDialog: AlertDialog
    private lateinit var pagerAdapter: SchedulePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpViews()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scheduleFlow.collect { pagerAdapter.submitList(it) }
            }
        }
        IsDataChanged.dataNotChanged()

    }

    fun setUpViews(){
        val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        appBar.post { appBar.setExpanded(false, false) }

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        viewPager = findViewById(R.id.viewPager2)
        pagerAdapter = SchedulePagerAdapter(
            addLesson = { schedule -> lessonAddDialog(schedule, null, false) },
            addTime = { item, scheduleItem ->
                addTimeDialog(item, scheduleItem)
            },
            deleteItem = { item, scheduleItem ->
                deleteTimeLesson(item, scheduleItem)
            },
            changeLesson = { item, isEven, scheduleItem ->
                lessonAddDialog(scheduleItem, item, isEven)
            }
        )

        viewPager.adapter = pagerAdapter


        viewPager.setPageTransformer { page, position ->
            page.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            when {
                position < -1 -> { // [-Infinity,-1)
                    page.alpha = 0f
                }

                position <= 0 -> { // [-1,0]
                    page.alpha = 1 + position
                    page.translationX = if (position == -1f) {
                        0f
                    } else {
                        page.width * -position * 0.5f
                    }
                    val scale = 1 - Math.abs(position) * 0.2f
                    page.scaleX = scale
                    page.scaleY = scale
                }

                position <= 1 -> { // (0,1]
                    page.alpha = 1 - position
                    page.translationX = if (position == 1f) {
                        0f
                    } else {
                        page.width * -position * 0.5f
                    }
                    val scale = 1 - Math.abs(position) * 0.2f
                    page.scaleX = scale
                    page.scaleY = scale
                }

                else -> { // (1,+Infinity]
                    page.alpha = 0f
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("onSupportNavigateUp", "onSupportNavigateUp pressed")
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Log.d("onBackPressed", "onBackPressed")
        if (IsDataChanged.getChanged()){
            Log.i("Is changed?", "YES!!")
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.save_changes))
                .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                    Log.d("saved", "saved")
                    viewModel.saveTempToSchedule()

                    val appWidgetManager = AppWidgetManager.getInstance(this)

                    val idsTrans = appWidgetManager.getAppWidgetIds(ComponentName(this,
                        DynamicWidProvider::class.java))

                    appWidgetManager.notifyAppWidgetViewDataChanged(idsTrans, R.id.listView)

                    super.onBackPressed()
                }
                .setNegativeButton(getString(R.string.cancel)){ dialog, _ ->
                    Log.d("not saved", "not saved")
                    viewModel.copyScheduleToTemp()
                    super.onBackPressed()

                }
                .create()
                .show()
        }
        else {
            Log.i("Is changed?", "NO!!")
            super.onBackPressed()
        }

    }

    fun deleteTimeLesson(item: TempLessonAndTime, schedule: TempSchedule){
        viewModel.deleteItem(item, schedule)
        IsDataChanged.dataChanged()


    }

    fun lessonAddDialog(scheduleDb: TempSchedule, lessonToChange: TempLessonAndTime?, isEven: Boolean){
        val dialogView = layoutInflater.inflate(R.layout.time_add_dialog, null)
        val addRecycler : RecyclerView = dialogView.findViewById(R.id.addRv)

        val title : TextView = dialogView.findViewById(R.id.tvTimeAdd)
        val subTitle : TextView = dialogView.findViewById(R.id.tvMessage)

        val btnClear : MaterialButton = dialogView.findViewById(R.id.btnClear)
        val addLesson : MaterialButton = dialogView.findViewById(R.id.add)
        val editLesson : MaterialButton = dialogView.findViewById(R.id.edit)
        val deleteLesson : MaterialButton = dialogView.findViewById(R.id.delete)

        title.text = "Lesson picker"
        subTitle.text = "Add a new lesson or choose from the list"

        if (lessonToChange != null){
            if (isEven && lessonToChange.lessonSchedeleOnEven.isNotEmpty()){
                btnClear.visibility = View.VISIBLE
            }
        }
        addLesson.setOnClickListener {
            addItem(null, null)
        }


        val addAdapter = AddLessonScheduleAdapter(
            addLesson = { clickedLesson ->
            addLesson(
                scheduleDb,
                clickedLesson,
                lessonToChange,
                isEven
            )
        },
            editLesson = {item ->
                addItem(getString(R.string.edit_lesson), item)
            },
            deleteLesson = { item ->
                deleteItem(item, null)
            })

        editLesson.setOnClickListener {
            addAdapter.editMode = !addAdapter.editMode
            if (addAdapter.editMode){
                addAdapter.deleteMode = false
                title.text = "Edit mode"
                subTitle.text = "Click on lesson to edit it"
            }
            else{
                title.text = "Lesson picker"
                subTitle.text = "Add a new lesson or choose from the list"
            }
            addAdapter.notifyDataSetChanged()
        }

        deleteLesson.setOnClickListener {
            addAdapter.deleteMode = !addAdapter.deleteMode
            if (addAdapter.deleteMode){
                addAdapter.editMode = false
                title.text = "Delete mode"
                subTitle.text = "Click on lesson to delete it"
            }
            else{
                title.text = "Lesson picker"
                subTitle.text = "Add a new lesson or choose from the list"
            }
            addAdapter.notifyDataSetChanged()
        }

        addRecycler.apply {
            adapter = addAdapter
            lifecycleScope.launch {
                viewModel.addedLessons.collect { addAdapter.submitList(it) }
            }
            layoutManager = LinearLayoutManager(context)
        }
        alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        btnClear.setOnClickListener {
            viewModel.clearEvenLesson(scheduleDb, lessonToChange!!)
            alertDialog.dismiss()
            IsDataChanged.dataChanged()
        }
    }

    fun deleteItem(lessonItem: LessonChange?, timeItem: TimeChange?){
        if (lessonItem != null){
            val mDiaolog = MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_this_lesson))
                .setMessage(getString(R.string.delete_confirm))
                .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                    viewModel.deleteLesson(lessonItem, null)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

            mDiaolog.show()
        }
        else{
            val mDiaolog = MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_this_time))
                .setMessage(getString(R.string.delete_confirm))
                .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                    viewModel.deleteLesson(null, timeItem)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

            mDiaolog.show()
        }

    }

    fun addItem(titleText: String?, item: LessonChange?){
        val dialogView = layoutInflater.inflate(R.layout.add_lesson_diaolog, null)
        val textLayout = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.lessonInput)
        val saveBtn = dialogView.findViewById<Button>(R.id.addButton)
        editText.setText((item?.lesson ?: "").toString())

        val alertDialog = MaterialAlertDialogBuilder(this)
            .setBackgroundInsetStart(32)
            .setBackgroundInsetEnd(32)
            .setTitle(titleText ?: getString(R.string.add_lesson))
            .setView(dialogView)
            .create()

//        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnShowListener {
            editText.requestFocus()
            editText.postDelayed({
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }
        editText.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrEmpty()){
                textLayout.error = null
                textLayout.isErrorEnabled = false
            }
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                saveBtn.performClick()
                true
            } else false
        }

        saveBtn.setOnClickListener {
            val newLesson = editText.text.toString()
            if (newLesson.isNullOrEmpty()){
                textLayout.error = getString(R.string.write_lesson)
                textLayout.isErrorEnabled = true
                return@setOnClickListener
            }
            viewModel.addItem(item, newLesson)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun addLesson(
        tempSchedule: TempSchedule,
        clickedLesson: LessonChange,
        lessonToChange: TempLessonAndTime?,
        isEven: Boolean
    ) {
        if (lessonToChange != null){
            viewModel.changeLesson(lessonToChange, tempSchedule, clickedLesson.lesson, isEven)
        }
        else{
            viewModel.lessonAdd(tempSchedule, clickedLesson.lesson)
        }
        alertDialog.dismiss()
        IsDataChanged.dataChanged()

    }

    fun addTimeDialog(item: TempLessonAndTime, schedule: TempSchedule){
        val dialogView = layoutInflater.inflate(R.layout.time_add_dialog, null)
        val addRecycler : RecyclerView = dialogView.findViewById(R.id.addRv)

        val title : TextView = dialogView.findViewById(R.id.tvTimeAdd)
        val subTitle : TextView = dialogView.findViewById(R.id.tvMessage)

        val clearItem : MaterialButton = dialogView.findViewById(R.id.btnClear)
        val addTime : MaterialButton = dialogView.findViewById(R.id.add)
        val editTime : MaterialButton = dialogView.findViewById(R.id.edit)
        val deleteTime : MaterialButton = dialogView.findViewById(R.id.delete)

        val defaultTitle = title.text
        val defaultSubTitle = subTitle.text

        val addAdapter = ChooseTimeAdapter(
            addTime = { rvItem -> addToDb(rvItem, item, schedule) },
            editTime = { item -> timeDialog(item)},
            deleteTime = {item -> deleteItem(null, item)}
        )

        addTime.setOnClickListener {
            timeDialog(null)
        }

        editTime.setOnClickListener {
            addAdapter.editMode = !addAdapter.editMode
            if (addAdapter.editMode){
                addAdapter.deleteMode = false
                title.text = "Edit mode"
                subTitle.text = "Click on time to edit it"
            }
            else{
                title.text = defaultTitle
                subTitle.text = defaultSubTitle
            }
            addAdapter.notifyDataSetChanged()
        }

        deleteTime.setOnClickListener {
            addAdapter.deleteMode = !addAdapter.deleteMode
            if (addAdapter.deleteMode){
                addAdapter.editMode = false
                title.text = "Delete mode"
                subTitle.text = "Click on time to delete it"
            }
            else{
                title.text = defaultTitle
                subTitle.text = defaultSubTitle
            }
            addAdapter.notifyDataSetChanged()
        }

        if (item.lessonStart.isNotEmpty()){
            clearItem.isVisible = true
            clearItem.text = "Clear time"
            clearItem.setOnClickListener {
                viewModel.clearTime(schedule, item)
                alertDialog.dismiss()
                IsDataChanged.dataChanged()
            }
        }

        addRecycler.adapter = addAdapter
        addRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        lifecycleScope.launch {
            viewModel.addedTime.collect { addAdapter.submitList(it) }

        }

        alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

    }

    fun addToDb(chosenItem: TimeChange, clickedLesson: TempLessonAndTime, schedule: TempSchedule){
        viewModel.addTime(chosenItem, clickedLesson, schedule)
        alertDialog.dismiss()
        IsDataChanged.dataChanged()
    }

    fun timeDialog(item: TimeChange? ){
        val hourStart = item?.lessonStartHour?.toInt()
        val minuteStart = item?.lessonStartMinute?.toInt()

        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(getString(R.string.start_time))
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hourStart ?: LocalTime.now().hour)
            .setMinute(minuteStart ?: 0)
            .build()


        timePicker.show(supportFragmentManager, "startTimePicker")

        timePicker.addOnPositiveButtonClickListener {
            endTimeDialog(timePicker.hour.toString(), timePicker.minute.toString(), item)
        }

    }

    private fun endTimeDialog(startHour: String, startMinute: String, item: TimeChange?) {
        val hourEnd = item?.lessonEndHour?.toInt()
        val minuteEnd = item?.lessonEndMinute?.toInt()

        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(getString(R.string.end_time))
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hourEnd ?: LocalTime.now().hour)
            .setMinute(minuteEnd?: 0)
            .build()
        timePicker.show(supportFragmentManager, "endTimePicker")

        timePicker.addOnPositiveButtonClickListener {
            viewModel.timeItem(
                item,
                startMinute,
                startHour,
                timePicker.minute.toString(),
                timePicker.hour.toString()
            )
        }
    }


//    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return true
//    }


}