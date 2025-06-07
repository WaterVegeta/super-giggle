package com.example.tabsgpttutor.data_base

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Schedule : RealmObject {
    @PrimaryKey
    var id : String = UUID.randomUUID().toString()
    var dayOfWeek: String = ""
    var lessonAndTime : RealmList<LessonAndTime> = realmListOf()
    var changeFuctor: Int = 0

}