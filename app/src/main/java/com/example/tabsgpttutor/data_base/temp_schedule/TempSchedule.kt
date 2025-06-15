package com.example.tabsgpttutor.data_base.temp_schedule

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class TempSchedule : RealmObject {
    @PrimaryKey
    var id : String = UUID.randomUUID().toString()
    var dayOfWeek: String = ""
    var lessonAndTime : RealmList<TempLessonAndTime> = realmListOf()
    var changeFuctor: Int = 0

}

