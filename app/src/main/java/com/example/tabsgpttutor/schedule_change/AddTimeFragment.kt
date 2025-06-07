package com.example.tabsgpttutor.schedule_change

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.SettingsViewModel
import com.example.tabsgpttutor.data_base.TimeChange
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import io.realm.kotlin.Realm
import kotlinx.coroutines.launch
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime

class AddTimeFragment: Fragment() {

    lateinit var realm: Realm
    lateinit var recyclerView: RecyclerView
    lateinit var rvAdapter: TimeChangeAdapter
    lateinit var btnSave: Button

    private val viewModel: SettingsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_lesson_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = MyDynamic.realm

        setUpViews(view)
    }


    fun setUpViews(view: View){
        recyclerView = view.findViewById(R.id.rvLesson)
        rvAdapter = TimeChangeAdapter(onEdit = { item ->
            editItem(item)
        },
            onDelete = {item ->
                deleteItem(item)
            })

        recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 250
                changeDuration = 250
                moveDuration = 250
                removeDuration = 250
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addedTime.collect { rvAdapter.submitList(it) }
            }
        }


        btnSave = view.findViewById(R.id.addLesson)
        btnSave.setOnClickListener {
            timeDialog(null, null, null, null, null)
        }

    }

    private fun deleteItem(item: TimeChange) {
        val mDiaolog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete this time?")
            .setMessage("Are you sure you want to delete this?")
            .setPositiveButton("Delete") { dialog, _ ->
                lifecycleScope.launch {
                    realm.write {
                        // Correct deletion syntax for Kotlin SDK
                        findLatest(item)?.let { delete(it) }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        mDiaolog.show()
    }

    private fun editItem(item: TimeChange) {
        timeDialog(item.lessonStartHour.toInt(), item.lessonStartMinute.toInt(), item.lessonEndHour.toInt(), item.lessonEndMinute.toInt(), item)
    }

    fun timeDialog(hourStart: Int?, minuteStart: Int?, hourEnd : Int?, minuteEnd: Int?, item: TimeChange? ){
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Start Time")
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hourStart ?: LocalTime.now().hour)
            .setMinute(minuteStart ?: 0)
            .build()


        timePicker.show(childFragmentManager, "timePicker")

        timePicker.addOnPositiveButtonClickListener {
            endTimeDialog(timePicker.hour.toString(), timePicker.minute.toString(), hourEnd, minuteEnd, item)
        }

    }

    private fun endTimeDialog(startHour: String, startMinute: String, hourEnd: Int?, minuteEnd: Int?, item: TimeChange?) {
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("End Time")
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hourEnd ?: LocalTime.now().hour)
            .setMinute(minuteEnd?: 0)
            .build()
        timePicker.show(childFragmentManager, "endTimePicker")

        timePicker.addOnPositiveButtonClickListener {
            lifecycleScope.launch {
                realm.write {
                    if (item != null){
                        findLatest(item)?.apply {
                            lessonStartHour = startHour
                            lessonStartMinute = startMinute
                            lessonEndHour = timePicker.hour.toString()
                            lessonEndMinute = timePicker.minute.toString()
                        }
                    } else {
                        val newTime = TimeChange().apply {
                            lessonStartHour = startHour
                            lessonStartMinute = startMinute
                            lessonEndHour = timePicker.hour.toString()
                            lessonEndMinute = timePicker.minute.toString()
                        }
                        copyToRealm(newTime)

                    }
                }
            }
        }
    }

}