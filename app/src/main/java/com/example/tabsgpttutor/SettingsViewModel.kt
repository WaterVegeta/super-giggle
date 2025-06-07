package com.example.tabsgpttutor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.tabsgpttutor.data_base.AnimationSettings
import com.example.tabsgpttutor.data_base.LessonAndTime
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.data_base.Schedule
import com.example.tabsgpttutor.data_base.TempLessonAndTime
import com.example.tabsgpttutor.data_base.TempSchedule
import com.example.tabsgpttutor.data_base.TimeChange
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.find
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map
import kotlin.collections.toTypedArray

class SettingsViewModel : ViewModel() {
    private val realm = MyDynamic.realm

    val addedLessons = realm.query<LessonChange>().sort("lesson", Sort.ASCENDING)
        .asFlow()
        .map {
            results ->
            results.list.toList()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )

    val addedTime = realm.query<TimeChange>()
        .asFlow()
        .map {
            results ->
            results.list.toList()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )



    val scheduleFlow = realm.query<TempSchedule>()
        .asFlow()
        .map { results ->
            results.list.toList()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(50000),
            emptyList()
        )


    fun firstSetUp(){
        viewModelScope.launch {
            realm.write {
                copyToRealm(TempSchedule().apply { dayOfWeek = "MONDAY" })
                copyToRealm(TempSchedule().apply { dayOfWeek = "TUESDAY" })
                copyToRealm(TempSchedule().apply { dayOfWeek = "WEDNESDAY" })
                copyToRealm(TempSchedule().apply { dayOfWeek = "THURSDAY" })
                copyToRealm(TempSchedule().apply { dayOfWeek = "FRIDAY" })
                copyToRealm(TempSchedule().apply { dayOfWeek = "SATURDAY" })
                copyToRealm(TempSchedule().apply { dayOfWeek = "SUNDAY" })

                copyToRealm(Schedule().apply { dayOfWeek = "MONDAY" })
                copyToRealm(Schedule().apply { dayOfWeek = "TUESDAY" })
                copyToRealm(Schedule().apply { dayOfWeek = "WEDNESDAY" })
                copyToRealm(Schedule().apply { dayOfWeek = "THURSDAY" })
                copyToRealm(Schedule().apply { dayOfWeek = "FRIDAY" })
                copyToRealm(Schedule().apply { dayOfWeek = "SATURDAY" })
                copyToRealm(Schedule().apply { dayOfWeek = "SUNDAY" })

                copyToRealm(AnimationSettings().apply { whatView = "Schedule" })
                copyToRealm(AnimationSettings().apply { whatView = "Homework" })
            }
        }
    }

    fun delete(sdeF: RealmObject){
        viewModelScope.launch {
            realm.write {
                delete(query(sdeF::class))
            }
        }
    }

//    fun replaceNames(){
//        viewModelScope.launch {
//            realm.write {
//                val data = query<Schedule>().find()
//                data.forEach {
//                    it.dayOfWeek = it.dayOfWeek.uppercase()
//                }
//            }
//        }
//    }

    fun clearEvenLesson(schedule: TempSchedule, lessonAndTime: TempLessonAndTime?){
        viewModelScope.launch {
            realm.write {
                findLatest(schedule)?.apply {
                    changeFuctor ++
                }
                if (lessonAndTime != null){
                    findLatest(lessonAndTime)?.let {
                        it.lessonSchedeleOnEven = ""
                    }

                }
            }
        }
    }

    fun changeLesson(lesson: TempLessonAndTime,
                     schedule: TempSchedule,
                     selectedLesson: String,
                     isEven: Boolean){
        viewModelScope.launch {
            realm.write {
                findLatest(schedule)?.apply {
                    changeFuctor ++
                }
                findLatest(lesson)?.apply {
                    if (isEven){
                        lessonSchedeleOnEven = selectedLesson
                    }
                    else{
                        lessonScheduleOnOdd = selectedLesson

                    }
                }
            }
        }

    }

    fun lessonAdd(item: TempSchedule, selectedLesson: String){
        viewModelScope.launch {
            realm.write {
                findLatest(item)?.apply {
                    changeFuctor ++
                }?.lessonAndTime?.add(
                    TempLessonAndTime().apply {
                        lessonScheduleOnOdd = selectedLesson
                    }
                )
            }
        }
    }

    fun addTime(chosenItem: TimeChange, clickedLesson: TempLessonAndTime, schedule: TempSchedule){
        viewModelScope.launch {
            val startMinute = when(chosenItem.lessonStartMinute){
                "0"->"00"
                "1"->"01"
                "2"->"02"
                "3"->"03"
                "4"->"04"
                "5"->"05"
                "6"->"06"
                "7"->"07"
                "8"->"08"
                "9"->"09"
                else -> chosenItem.lessonStartMinute
            }
            realm.write {
                findLatest(clickedLesson)?.apply {
                    lessonStart = chosenItem.lessonStartHour + ":" + startMinute
                    lessonEndHour = chosenItem.lessonEndHour
                    lessonEndMinute = chosenItem.lessonEndMinute
                }
                findLatest(schedule)?.apply {
                    changeFuctor ++
                }
            }
        }
    }

    fun deleteItem(item: TempLessonAndTime, schedule: TempSchedule){
        viewModelScope.launch {
            realm.write {
                findLatest(item)?.let {
                    Log.d("deleted", "deleted${it.lessonScheduleOnOdd}")
                    delete(it)
                }
                findLatest(schedule)?.apply {
                    Log.d("change", "changeFuctor: $changeFuctor")
                    changeFuctor = changeFuctor + 1
                    Log.d("change", "changeFuctor: $changeFuctor schedule: ${schedule.dayOfWeek}")
                }
            }
        }
    }

    fun copyScheduleToTemp() {
        viewModelScope.launch {
            realm.write {
                delete(query<TempSchedule>())
                val schedules = query<Schedule>().find()
                schedules.forEach { item ->
                    val temp = TempSchedule().apply {
                        id = item.id
                        dayOfWeek = item.dayOfWeek
                        changeFuctor = item.changeFuctor
                        lessonAndTime = realmListOf(*item.lessonAndTime.map {
                            TempLessonAndTime().apply {
                                objectId = it.objectId
                                lessonStart = it.lessonStart
                                lessonEndHour = it.lessonEndHour
                                lessonEndMinute = it.lessonEndMinute
                                lessonScheduleOnOdd = it.lessonScheduleOnOdd
                                lessonSchedeleOnEven = it.lessonSchedeleOnEven
                            }
                        }.toTypedArray())
                    }
                    copyToRealm(temp)
                }
            }

        }
    }

    fun saveTempToSchedule() {
        viewModelScope.launch {
            realm.write {
                delete(query<Schedule>())
                val temps = query<TempSchedule>().find()

                temps.forEach { temp ->
                    val original = Schedule().apply {
                        id = temp.id
                        dayOfWeek = temp.dayOfWeek
                        changeFuctor = temp.changeFuctor

                        lessonAndTime = realmListOf(
                            *temp.lessonAndTime.map {
                                LessonAndTime().apply {
                                    objectId = it.objectId
                                    lessonStart = it.lessonStart
                                    lessonEndHour = it.lessonEndHour
                                    lessonEndMinute = it.lessonEndMinute
                                    lessonScheduleOnOdd = it.lessonScheduleOnOdd
                                    lessonSchedeleOnEven = it.lessonSchedeleOnEven
                                }
                            }.toTypedArray()
                        )
                    }

                    copyToRealm(original)
                }
            }

        }
    }
}