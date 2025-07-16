package com.example.tabsgpttutor.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import java.time.LocalDate
import java.time.LocalTime

class ScheduleWidgetService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        return ScheduleWidgetFactory(
            applicationContext,
            appWidgetId)
    }
}