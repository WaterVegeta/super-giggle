package com.example.task_king.data_base

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class TimeChange: RealmObject {
    @PrimaryKey
    var id : String = UUID.randomUUID().toString()
    var lessonStartHour: String = ""
    var lessonEndHour: String = ""
    var lessonStartMinute: String = ""
    var lessonEndMinute: String = ""
}