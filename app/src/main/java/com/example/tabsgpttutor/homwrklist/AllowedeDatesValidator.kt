package com.example.tabsgpttutor.homwrklist

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints
import java.time.Instant
import java.time.ZoneId

class AllowedDatesValidator(private val allowedDates: Set<Long>) : CalendarConstraints.DateValidator {
    override fun isValid(date: Long): Boolean {
        // Convert both dates to start of day in system default timezone for accurate comparison
        val inputDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault())
            .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return allowedDates.any { allowedDate ->
            val allowedDateStart = Instant.ofEpochMilli(allowedDate).atZone(ZoneId.systemDefault())
                .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            inputDate == allowedDateStart
        }
    }

    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int) = Unit
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<AllowedDatesValidator> {
            override fun createFromParcel(source: Parcel) = AllowedDatesValidator(emptySet())
            override fun newArray(size: Int) = arrayOfNulls<AllowedDatesValidator>(size)
        }
    }
}