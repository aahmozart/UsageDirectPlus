package godau.fynn.usagedirect.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.widget.TimeTodayProvider;

public class TimeTodayWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int widgetId : appWidgetIds) {
            update(context, appWidgetManager, widgetId);
        }
    }

    private void update(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_time_today);

        int height = appWidgetManager.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT); // in dip
        int width = appWidgetManager.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH); // in dip

        float textSize = (height - 12 - 16 - 16) / 2; // in dip

        float ratio = width / textSize;

        //Log.v("WIDGET", "ration of " + widgetId + " is " + ratio);

        Accuracy accuracy;

        if (ratio < 1.75 | width < 100) {
            accuracy = Accuracy.HOUR_ONLY;
        } else if (ratio < 2.75) {
            accuracy = Accuracy.FRENCH_HOUR;
        } else if (ratio < 4.5) {
            accuracy = Accuracy.MINUTE;
        } else {
            accuracy = Accuracy.SECOND;
        }

        String text;

        long timeToday = TimeTodayProvider.getTimeToday(context);
        long secondsToday = timeToday / 1000;

        switch (accuracy) {
            case HOUR_ONLY:
                text = String.valueOf(secondsToday / 60 / 60);
                break;
            case FRENCH_HOUR:
                text = context.getString(R.string.widget_today_hour_french, (secondsToday / 60 / 60));
                break;
            case MINUTE:
                text = context.getString(
                        R.string.widget_today_minute,
                        secondsToday / 60 / 60, (secondsToday / 60) % 60
                );
                break;
            case SECOND:
            default:
                text = context.getString(
                        R.string.widget_today_second,
                        secondsToday / 60 / 60, (secondsToday / 60) % 60, secondsToday % 60
                );
        }

        views.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_DIP, textSize);
        views.setTextViewText(R.id.widget_text, text);

        // Refresh on click
        Intent refreshIntent = new Intent(context, TimeTodayWidgetProvider.class);
        refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = new int[]{widgetId};
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        appWidgetManager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        update(context, appWidgetManager, appWidgetId);
    }

    private enum Accuracy {
        HOUR_ONLY, FRENCH_HOUR, MINUTE, SECOND
    }
}
