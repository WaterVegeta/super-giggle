package com.example.tabsgpttutor.shcedule.testing


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.data_base.Homework
import com.example.tabsgpttutor.data_base.shedule.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class TestViewModel(private val dayOfWeek: String,
    private val currentDate: LocalDate) : ViewModel() {
    private val realm = MyDynamic.realm

    private val _uiState = MutableStateFlow<List<TestData>>(emptyList())
    val uiState: StateFlow<List<TestData>> = _uiState

    init {
        val evenWeek = isEvenWeek(currentDate)
        val formattedDate = currentDate.toString() // "yyyy-MM-dd"

        val scheduleFlow = realm
            .query(Schedule::class, "dayOfWeek == $0", dayOfWeek)
            .asFlow()
            .map { it.list.firstOrNull() }

        val homeworkFlow = realm
            .query(Homework::class, "date == $0", formattedDate)
            .asFlow()
            .map { it.list }

        viewModelScope.launch {
            combine(scheduleFlow, homeworkFlow) { schedule, homeworks ->
                schedule?.lessonAndTime?.map { lesson ->
                    val lessonName = if (evenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty()) lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd
                    val lessonTime = "${lesson.lessonStart} - ${lesson.lessonEndHour}:${lesson.lessonEndMinute}"
                    val hw = homeworks.find { it.lesson == lessonName }

                    TestData(
                        objectId = lesson.objectId,
                        lessonTime = lessonTime,
                        lessonName = lessonName,
                        homeworkNote = hw?.note,
                        isDone = hw?.done ?: false
                    )
                } ?: emptyList()
            }.collect { uiModels ->
                _uiState.value = uiModels
            }
        }
    }

    private fun isEvenWeek(date: LocalDate): Boolean {
        val weekOfYear = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
        return weekOfYear % 2 == 0
    }
}