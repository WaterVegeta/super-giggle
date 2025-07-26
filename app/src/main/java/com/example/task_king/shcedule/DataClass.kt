package com.example.task_king.shcedule

import java.util.UUID

data class DataClass(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val time: String,
    val homework: String? = null,
    val hwId: String,
    val done: Boolean?
){
    // For better diffing
    fun contentEquals(other: DataClass): Boolean {
        return homework == other.homework &&
                done == other.done &&
                subject == other.subject &&
                time == other.time
    }
}