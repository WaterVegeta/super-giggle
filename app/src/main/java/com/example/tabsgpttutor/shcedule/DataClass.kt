package com.example.tabsgpttutor.shcedule

import java.net.IDN

data class DataClass(
    val subject: String,
    val time: String,
    val homework: String? = null,
    val id: String,
    val done: Boolean?
)