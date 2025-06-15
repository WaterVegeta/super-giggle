package com.example.tabsgpttutor.data_base.temp_schedule

import io.realm.kotlin.types.RealmObject
import java.util.UUID

class TempLessonAndTime : RealmObject{
    var objectId: String = UUID.randomUUID().toString()
    var lessonStart: String = ""
    var lessonEndHour: String = ""
    var lessonEndMinute: String = ""
    var lessonScheduleOnOdd: String = ""
    var lessonSchedeleOnEven: String = ""
}