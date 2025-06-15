package com.example.tabsgpttutor.data_base

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Homework: RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var date: String = ""  // LocalDate as string
    var lesson: String = ""
    var note: String = ""
    var done: Boolean = false
    var images: RealmList<ImageItem> = realmListOf()
}