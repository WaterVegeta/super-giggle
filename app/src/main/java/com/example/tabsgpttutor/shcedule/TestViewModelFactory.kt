package com.example.tabsgpttutor.shcedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate

class TestViewModelFactory(
    private val dayOfWeek: String,
    private val currentDate: LocalDate
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel(dayOfWeek, currentDate) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}