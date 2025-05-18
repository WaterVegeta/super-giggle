package com.example.tabsgpttutor.shcedule

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.tabsgpttutor.Homework
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.ceil

class ScheduleFrag: Fragment(R.layout.schedule_frag_layout) {

    lateinit var viewPager: ViewPager2
    val OFFSET = 1000
    lateinit var calendarFAB: FloatingActionButton
    lateinit var deleteFAB: FloatingActionButton
    lateinit var homeFAB: ExtendedFloatingActionButton
    var localDate = LocalDate.now()
    var localTime = LocalTime.now()
    var k = 0
    lateinit var realm: Realm
    var curPosition = 0
    lateinit var currentDay: LocalDate
    lateinit var animShow: Animation
    lateinit var animHide: Animation
    lateinit var firstNoteText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.schedule_frag_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        realm = MyDynamic.Companion.realm

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = DayPagerAdapter(this)
        when (localDate.dayOfWeek.toString()) {
            "MONDAY" -> if (localTime.hour >= 15 && localTime.minute >= 5) {
                viewPager.setCurrentItem(OFFSET + 1, false)
                k++
            } else {
                viewPager.setCurrentItem(OFFSET, false)
            }

            "TUESDAY" -> if (localTime.hour >= 16 && localTime.minute >= 0) {
                viewPager.setCurrentItem(OFFSET + 1, false)
                k++
            } else {
                viewPager.setCurrentItem(OFFSET, false)
            }

            "WEDNESDAY" -> if (localTime.hour >= 15 && localTime.minute >= 5) {
                viewPager.setCurrentItem(OFFSET + 1, false)
                k++
            } else {
                viewPager.setCurrentItem(OFFSET, false)
            }

            "THURSDAY" -> if (localTime.hour >= 16 && localTime.minute >= 0) {
                viewPager.setCurrentItem(OFFSET + 1, false)
                k++
            } else {
                viewPager.setCurrentItem(OFFSET, false)
            }

            "FRIDAY" -> if (localTime.hour >= 14 && localTime.minute >= 10) {
                viewPager.setCurrentItem(OFFSET + 3, false)
                k++
                k++
                k++
            } else {
                viewPager.setCurrentItem(OFFSET, false)
            }

            "SATURDAY" -> {
                viewPager.setCurrentItem(OFFSET + 2, false)
                k++
                k++
            }

            "SUNDAY" -> {
                viewPager.setCurrentItem(OFFSET + 1, false)
                k++
            }

            else -> viewPager.setCurrentItem(OFFSET, false)

        }
//        viewPager.offscreenPageLimit = 2

        calendarFAB = view.findViewById(R.id.calendarFAB)
        calendarFAB.setOnClickListener {
            quickDateChange()
        }
        homeFAB = view.findViewById(R.id.homeFAB)
        homeFAB.setOnClickListener {
            viewPager.setCurrentItem(OFFSET + k, true)
            homeFAB.hide()

        }
        curPosition = OFFSET + k

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateHomeButton(position)
                if (calendarFAB.isShown == false){
                    calendarFAB.apply {
                        startAnimation(animShow)
                        show()
                    }

                }



                curPosition = position
            }
        })

        animShow = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_animation)
        calendarFAB.startAnimation(animShow)
        animHide = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_anim_hide)





    }

    fun hideFABs() {
        calendarFAB.hide()
        homeFAB.hide()
    }

    fun showFABs() {
        if (calendarFAB.isShown == false ){

            calendarFAB.apply {
                startAnimation(animShow)
                show()
            }
            if (curPosition == OFFSET + k) {
                homeFAB.apply {
                    hide()
                }
            }
            else {
                homeFAB.apply {
                    startAnimation(animShow)
                    show()
                }
            }
        }

    }

    fun quickDateChange() {
        val dialogView = layoutInflater.inflate(R.layout.bsheet_calendar_change, null)
        var dateInput = dialogView.findViewById<CalendarView>(R.id.calendarView)
        val changeButton = dialogView.findViewById<Button>(R.id.changeButton)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)

        var selectedDate = LocalDate.now()
        dateInput.date = selectedDate.plusDays((viewPager.currentItem - OFFSET).toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        dateInput.setOnDateChangeListener { _, year, month, day ->
            selectedDate = LocalDate.of(year, month + 1, day)
        }

        changeButton.setOnClickListener {
            val today = LocalDate.now()
            val targetPosition = OFFSET + (selectedDate.toEpochDay() - today.toEpochDay()).toInt()
            viewPager.setCurrentItem(targetPosition, true)
            bottomSheetDialog.dismiss()
            updateHomeButton(targetPosition)
        }
        bottomSheetDialog.show()
    }

    fun updateHomeButton(position: Int) {
        if (position == OFFSET + k) {
            homeFAB.hide()
        } else if (homeFAB.isShown == false) {
            homeFAB.apply {
                startAnimation(animShow)
                show()
            }
        }
    }

    fun doneHw(fGDate: LocalDate, fGnote: String, fGsubject: String, position: Int){
        val fragPosition = viewPager.currentItem
        val fragTag = (viewPager.adapter as DayPagerAdapter).getFragmentTag(fragPosition)
        val refFrag = childFragmentManager.findFragmentByTag(fragTag) as? DayFragment
        val homework = realm.query<Homework>("date == $0 AND note == $1 AND lesson == $2",
            fGDate.toString(), fGnote, fGsubject).first().find()

        var timeList = resources.getStringArray(R.array.six)
        var thatTime = timeList[position]

        lifecycleScope.launch {
            realm.write {
                if (homework != null) {
                    findLatest(homework)?.done = true
                }
            }
            if (refFrag != null) {
                refFrag.refreshData(fGDate, fGsubject, thatTime, position)
                Log.d("FragmentFound", "tag of frag: $refFrag, $fragTag")
            }
        }
    }

    fun nextSubjectDate(subject: String, FGdate: LocalDate, pressedPosition: Int) {
        val today = FGdate
        for (i in 1..60) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek
            val weekNumber = ceil(date.dayOfYear / 7.0).toInt()
            val isEvenWeek = weekNumber % 2 == 0

            val lessons = when (dayOfWeek) {
                DayOfWeek.MONDAY -> if (isEvenWeek) resources.getStringArray(R.array.monday1) else resources.getStringArray(
                    R.array.monday2
                )

                DayOfWeek.TUESDAY -> resources.getStringArray(R.array.tues)
                DayOfWeek.WEDNESDAY -> resources.getStringArray(R.array.wend)
                DayOfWeek.THURSDAY -> resources.getStringArray(R.array.thurs)
                DayOfWeek.FRIDAY -> resources.getStringArray(R.array.frid)
                else -> continue
            }

            if (lessons.contains(subject)) {
                showAddHomeworkDialog(subject, date, pressedPosition, i, lessons)
                break
            }
        }
    }

    fun showAddHomeworkDialog(
        subject: String,
        sDate: LocalDate,
        position: Int,
        toDay: Int,
        lessons: Array<String>
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_homework, null)
        val input = dialogView.findViewById<TextInputEditText>(R.id.noteInput)
        val txtLayout = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val button = dialogView.findViewById<Button>(R.id.addButton)
        val toggleGroup = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        input.requestFocus()

//        input.doOnTextChanged {text, start, before, count ->
//            if (text.isNullOrEmpty()){
//                txtLayout.error = "Write something"
//            }else{
//                txtLayout.error = null
//            }
//        }

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)
        val isHomewrkExists =
            realm.query<Homework>("date == $0 AND lesson == $1", sDate.toString(), subject).first()
                .find()

        var timeList = resources.getStringArray(R.array.six)
        var thatTime = timeList[position]


        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.addNewButton -> {
                        firstNoteText = input.text.toString()
                        val fragPosition = viewPager.currentItem + toDay
                        val fragTag = (viewPager.adapter as DayPagerAdapter).getFragmentTag(fragPosition)
                        Log.d("MainActivity", "currentItem: $fragPosition and tag $fragTag")

                        val refFrag = childFragmentManager.findFragmentByTag(fragTag) as? DayFragment
                        lifecycleScope.launch {
                            val noteTextValue: String? = realm.write {
                                if (isHomewrkExists != null) {
                                    findLatest(isHomewrkExists)?.note
                                } else firstNoteText
                            }
                            input.setText(noteTextValue ?: "")
                        }

                        button.setOnClickListener {
                            var noteText = input.text.toString()
                            if (noteText.isEmpty()){
                                txtLayout.error = "Write homework or leave"
                                return@setOnClickListener
                            }
                            lifecycleScope.launch {
                                realm.write {
                                    if (isHomewrkExists != null) {
                                        findLatest(isHomewrkExists)?.note = noteText
                                    } else {
                                        val newItem = Homework().apply {
                                            lesson = subject
                                            date = sDate.toString()
                                            note = noteText
                                        }
                                        copyToRealm(newItem)
                                    }
                                }

                                bottomSheetDialog.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    "Homework added for $sDate",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val allHomework = realm.query<Homework>(
                                    "date == $0 AND lesson == $1",
                                    sDate.toString(), subject
                                ).first().find()?.note
                                Log.d("HomeworkDatasds", "data: $allHomework")

                                if (refFrag != null) {
                                    refFrag.refreshData(sDate, subject, thatTime, position)
                                    Log.d("FragmentFound", "tag of frag: $refFrag, $fragTag")
                                }
                                viewPager.setCurrentItem(viewPager.currentItem, true)
                            }
                        }

                    }

                    R.id.editDayButton -> {
                        firstNoteText = input.text.toString()
                        val fragPosition = viewPager.currentItem
                        val fragTag = (viewPager.adapter as DayPagerAdapter).getFragmentTag(fragPosition)
                        Log.d("MainActivity", "currentItem: $fragPosition and tag $fragTag")

                        val refFrag = childFragmentManager.findFragmentByTag(fragTag) as? DayFragment
                        currentDay =
                            LocalDate.now().plusDays((viewPager.currentItem - OFFSET).toLong())
                        Log.d("CurrentDate", "date: $currentDay")
                        var week = ceil(currentDay.dayOfYear / 7.0)
                        var currentLessons = when(currentDay.dayOfWeek) {
                            DayOfWeek.MONDAY -> if (week % 2 == 0.0) resources.getStringArray(R.array.monday1)
                            else resources.getStringArray(R.array.monday2)
                            DayOfWeek.TUESDAY -> resources.getStringArray(R.array.tues)
                            DayOfWeek.WEDNESDAY -> resources.getStringArray(R.array.wend)
                            DayOfWeek.THURSDAY -> resources.getStringArray(R.array.thurs)
                            DayOfWeek.FRIDAY -> resources.getStringArray(R.array.frid)
                            else -> arrayOf("")
                        }
                        val isHomewrkExists = realm.query<Homework>("date == $0 AND lesson == $1", currentDay.toString(), subject).first()
                            .find()
                        lifecycleScope.launch {
                            val noteTextValue: String? = realm.write {
                                if (isHomewrkExists != null) {
                                    findLatest(isHomewrkExists)?.note
                                } else firstNoteText
                            }
                            input.setText(noteTextValue ?: "")
                        }

                        button.setOnClickListener {
                            var noteText = input.text.toString()
                            if (noteText.isEmpty()){
                                txtLayout.error = "Write homework or leave"
                                return@setOnClickListener
                            }
                            lifecycleScope.launch {
                                realm.write {
                                    if (isHomewrkExists != null) {
                                        findLatest(isHomewrkExists)?.note = noteText
                                    } else {
                                        val newItem = Homework().apply {
                                            lesson = subject
                                            date = currentDay.toString()
                                            note = noteText
                                        }
                                        copyToRealm(newItem)
                                    }
                                }

                                bottomSheetDialog.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    "Homework added for $currentDay",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val allHomework = realm.query<Homework>(
                                    "date == $0 AND lesson == $1",
                                    currentDay.toString(), subject
                                ).first().find()?.note
                                Log.d("HomeworkDatasds", "data: $allHomework")

                                if (refFrag != null) {
                                    refFrag.refreshData(currentDay, subject, thatTime, position)
                                    Log.d("FragmentFound", "tag of frag: $refFrag, $fragTag")
                                }
                            }
                        }
                    }
                }
            }
        }
        bottomSheetDialog.show()
        toggleGroup.check(R.id.addNewButton)
    }

}