package com.example.tabsgpttutor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
//import io.realm.kotlin.ext.freeze

class HwViewModel: ViewModel() {
    private val realm = MyDynamic.realm

    private val _homework = MutableStateFlow<List<Homework>>(emptyList())

//    val homework: StateFlow<List<Homework>> = _homework
    val homework = realm.query<Homework>("date >= $0", LocalDate.now().toString())
    .sort("date", Sort.ASCENDING)
    .asFlow()
    .map { results ->
        results.list.toList()
    }
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    val homeworkList = realm.query<Homework>("date >= $0", LocalDate.now().toString())
        .sort("date", Sort.ASCENDING)
        .asFlow()
        .map { results ->
            results.list.toList()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
     )
    var sada = Log.d("ViewModel", "model hw: $homeworkList")




    fun addHw(date: String, note: String, lesson: String){
        viewModelScope.launch {
            realm.write {
                copyToRealm(Homework().apply {
                    this.date = date
                    this.note = note
                    this.lesson = lesson
                })
            }
        }
    }
    fun doneHw(hw: Homework){
        viewModelScope.launch {
            realm.write {
                findLatest(hw)?.done = true
            }
        }
    }

    fun deleteHw(ids: List<String>) {
        viewModelScope.launch {
            realm.write {
                ids.forEach { id ->
                    Log.d("ViewModel", "deleting id: $id")
                    query<Homework>("id == $0", id)
                        .first()
                        .find()
                        ?.let {
                            delete(it)
                            Log.d("ViewModel", "deleted: $it")
                        }
                }
            }
        }
    }

    fun editHw(
        date: String,
        note: String,
        lesson: String,
        newDate: String,
        newNote: String,
        newLesson: String){
        viewModelScope.launch {
            realm.write {
                // More efficient direct query
                query<Homework>(
                    "date == $0 AND note == $1 AND lesson == $2",
                    date,
                    note,
                    lesson
                )
                    .first()
                    .find()
                    ?.let { hw ->
                        findLatest(hw)?.apply {
                            this.date = newDate
                            this.note = newNote
                            this.lesson = newLesson
                        }
                    }
            }

        }


    }

    suspend fun findHwById(itemId: String): Homework?{
        return realm.write {
            query<Homework>("id == $0", itemId).first().find()
        }
    }
}