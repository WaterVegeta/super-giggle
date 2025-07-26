package com.example.task_king.data_base.shedule

import io.realm.kotlin.types.RealmObject
import java.util.UUID

class LessonAndTime: RealmObject {
    var objectId: String = UUID.randomUUID().toString()
    var lessonStart: String = ""
    var lessonEndHour: String = ""
    var lessonEndMinute: String = ""
    var lessonScheduleOnOdd: String = ""
    var lessonSchedeleOnEven: String = ""
}