package com.example.tabsgpttutor.data_base

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class ImageItem: RealmObject {
    @PrimaryKey
    var id : String = UUID.randomUUID().toString()
    var imageUri : String = ""
}