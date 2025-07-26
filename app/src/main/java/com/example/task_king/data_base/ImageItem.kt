package com.example.task_king.data_base

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class ImageItem: RealmObject {
    @PrimaryKey
    var id : String = UUID.randomUUID().toString()
    var imageUri : String = ""
    var fileName: String = ""
    var filePath: String? = null
}