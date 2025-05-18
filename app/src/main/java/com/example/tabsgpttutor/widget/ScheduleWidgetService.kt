package com.example.tabsgpttutor.widget

import android.content.Intent
import android.widget.RemoteViewsService
import java.time.LocalDate
import java.time.LocalTime

class ScheduleWidgetService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ScheduleWidgetFactory(applicationContext)
    }
}