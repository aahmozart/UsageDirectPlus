package godau.fynn.usagedirectplus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.wrapper.EventLogWrapper
import godau.fynn.usagedirectplus.wrapper.UsageStatsManagerWrapper

class TimeTodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val timeToday = getTimeToday(context)
        for (widgetId in appWidgetIds) {
            update(context, appWidgetManager, widgetId, timeToday)
        }
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int, timeToday: Long) {
        val views = RemoteViews(context.packageName, R.layout.widget_time_today)

        val height = appWidgetManager.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) // in dip
        val width = appWidgetManager.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) // in dip

        val textSize = (height - 12 - 16 - 16).toFloat() / 2 // in dip

        val ratio = width / textSize

        val accuracy = when {
            ratio < 1.75f || width < 100 -> Accuracy.HOUR_ONLY
            ratio < 2.75f -> Accuracy.FRENCH_HOUR
            ratio < 4.5f -> Accuracy.MINUTE
            else -> Accuracy.SECOND
        }

        val secondsToday = timeToday / 1000

        val text = when (accuracy) {
            Accuracy.HOUR_ONLY ->
                (secondsToday / 60 / 60).toString()
            Accuracy.FRENCH_HOUR ->
                context.getString(R.string.widget_today_hour_french, secondsToday / 60 / 60)
            Accuracy.MINUTE ->
                context.getString(
                    R.string.widget_today_minute,
                    secondsToday / 60 / 60, (secondsToday / 60) % 60
                )
            Accuracy.SECOND ->
                context.getString(
                    R.string.widget_today_second,
                    secondsToday / 60 / 60, (secondsToday / 60) % 60, secondsToday % 60
                )
        }

        views.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_DIP, textSize)
        views.setTextViewText(R.id.widget_text, text)

        // Refresh on click
        val refreshIntent = Intent(context, TimeTodayWidgetProvider::class.java)
        refreshIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = intArrayOf(widgetId)
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        update(context, appWidgetManager, appWidgetId, getTimeToday(context))
    }

    private enum class Accuracy {
        HOUR_ONLY, FRENCH_HOUR, MINUTE, SECOND
    }

    companion object {
        private fun getTimeToday(context: Context): Long {
            val eventLogWrapper = EventLogWrapper(context)

            return UsageStatsManagerWrapper.aggregateSimpleUsageStats(
                eventLogWrapper.aggregateForegroundStats(
                    eventLogWrapper.getForegroundStatsByRelativeDay(0), null
                )
            )
        }
    }
}
