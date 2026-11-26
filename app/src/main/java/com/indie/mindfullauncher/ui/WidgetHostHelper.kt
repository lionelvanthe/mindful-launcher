package com.indie.mindfullauncher.ui

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

object WidgetHostHelper {
    const val HOST_ID = 1024

    @Volatile
    private var host: AppWidgetHost? = null

    fun getHost(context: Context): AppWidgetHost {
        return host ?: synchronized(this) {
            host ?: AppWidgetHost(context.applicationContext, HOST_ID).also {
                it.startListening()
                host = it
            }
        }
    }

    fun createPickIntent(context: Context): Pair<Int, Intent> {
        val appWidgetHost = getHost(context)
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return appWidgetId to intent
    }

    fun deleteWidgetId(context: Context, appWidgetId: Int) {
        if (appWidgetId > 0) {
            runCatching { getHost(context).deleteAppWidgetId(appWidgetId) }
        }
    }

    fun createWidgetView(context: Context, appWidgetId: Int): AppWidgetHostView? {
        if (appWidgetId <= 0) return null
        val manager = AppWidgetManager.getInstance(context)
        val info = manager.getAppWidgetInfo(appWidgetId) ?: return null
        return runCatching {
            getHost(context).createView(context, appWidgetId, info).apply {
                setAppWidget(appWidgetId, info)
            }
        }.getOrNull()
    }
}
