package com.example.tabsgpttutor.settings.schedule_change.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.SettingsViewModel
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.data_base.temp_schedule.TempLessonAndTime
import com.example.tabsgpttutor.data_base.temp_schedule.TempSchedule
import com.example.tabsgpttutor.data_base.TimeChange
import com.example.tabsgpttutor.settings.schedule_change.IsDataChanged
import com.example.tabsgpttutor.settings.schedule_change.adapters.AddLessonScheduleAdapter
import com.example.tabsgpttutor.settings.schedule_change.adapters.ChildScheduleAdapter
import com.example.tabsgpttutor.settings.schedule_change.adapters.ChooseTimeAdapter
import com.example.tabsgpttutor.settings.schedule_change.adapters.SchedulePagerAdapter
import kotlinx.coroutines.launch

class AddScheduleFragment: Fragment(R.layout.add_schedule_fragment) {

    private val viewModel: SettingsViewModel by viewModels()
    lateinit var viewPager: ViewPager2
    lateinit var alertDialog: AlertDialog
    private lateinit var pagerAdapter: SchedulePagerAdapter
    lateinit var recyclerView: RecyclerView
//    lateinit var parentAdapter: ParentScheduleAdapter
    lateinit var childAdapter: ChildScheduleAdapter
    lateinit var lessons : Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        copyScheduleToTemp(MyDynamic.realm)
        viewPager = view.findViewById(R.id.viewPager2)

//        viewModel.delete()
//        viewModel.firstSetUp()

        pagerAdapter = SchedulePagerAdapter(
            { schedule -> lessonAddDialog(schedule, null, false) },
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
//        lifecycleScope.launch {
//            realm.write {
//                copyToRealm(Schedule().apply { dayOfWeek = "Monday" })
//                copyToRealm(Schedule().apply { dayOfWeek = "Tuesday" })
//                copyToRealm(Schedule().apply { dayOfWeek = "Wednesday" })
//                copyToRealm(Schedule().apply { dayOfWeek = "Thursday" })
//                copyToRealm(Schedule().apply { dayOfWeek = "Friday" })
//            }
//        }
        viewPager.adapter = pagerAdapter


        viewPager.setPageTransformer { page, position ->
            page.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            when {
                position < -1 -> { // [-Infinity,-1)
                    page.alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    page.alpha = 1 + position
                    page.translationX = if (position == -1f){
                        0f
                    } else{
                        page.width * -position *0.5f
                    }
                    val scale = 1 - Math.abs(position) * 0.2f
                    page.scaleX = scale
                    page.scaleY = scale
                }
                position <= 1 -> { // (0,1]
                    page.alpha = 1 - position
                    page.translationX = if (position == 1f){
                        0f
                    } else{
                        page.width * -position *0.5f
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scheduleFlow.collect { pagerAdapter.submitList(it) }
            }
        }
        IsDataChanged.dataNotChanged()

    }

    fun deleteTimeLesson(item: TempLessonAndTime, schedule: TempSchedule){
        viewModel.deleteItem(item, schedule)
        IsDataChanged.dataChanged()


    }

    fun lessonAddDialog(scheduleDb: TempSchedule, lessonToChange: TempLessonAndTime?, isEven: Boolean){
        val dialogView = layoutInflater.inflate(R.layout.time_add_dialog, null)
        val addRecycler : RecyclerView = dialogView.findViewById(R.id.addRv)
        val btnClear : Button = dialogView.findViewById(R.id.btnClear)
        if (isEven){
            btnClear.visibility = View.VISIBLE
        }
        val addAdapter = AddLessonScheduleAdapter(addLesson = { clickedLesson ->
            addLesson(
                scheduleDb,
                clickedLesson,
                lessonToChange,
                isEven
            )
        })
        addRecycler.apply {
            adapter = addAdapter
            lifecycleScope.launch {
                viewModel.addedLessons.collect { addAdapter.submitList(it) }
            }
            layoutManager = LinearLayoutManager(requireContext())
        }
        alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        btnClear.setOnClickListener {
            viewModel.clearEvenLesson(scheduleDb, lessonToChange)
            alertDialog.dismiss()
            IsDataChanged.dataChanged()
        }
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
        val addAdapter = ChooseTimeAdapter(addTime = { rvItem -> addToDb(rvItem, item, schedule) })
        addRecycler.adapter = addAdapter
        addRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        lifecycleScope.launch {
            viewModel.addedTime.collect { addAdapter.submitList(it) }

        }

        alertDialog = AlertDialog.Builder(requireContext())
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




    override fun onResume() {
        super.onResume()
//        viewPager.setCurrentItem(6, true)
//        viewPager.setCurrentItem(0, true)
    }

}