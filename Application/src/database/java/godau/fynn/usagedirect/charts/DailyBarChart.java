package godau.fynn.usagedirect.charts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.room.Room;
import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.UsageStatsDao;

import java.util.Map;

import static godau.fynn.usagedirect.persistence.HistoryDatabase.DATABASE_NAME;

public class DailyBarChart extends UsageStatBarChart {

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {

        setText(getText());

        new Thread(new Runnable() {
            @Override
            public void run() {

                UsageStatsDao usageStats = HistoryDatabase.getUsageStatsDao(getContext());

                final Map<Long, Long> usagePerDayMap = usageStats.getTotalTimePerDay();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onDataLoaded(usagePerDayMap);
                    }
                });
            }
        }).start();
    }

    protected @StringRes
    int getText() {
        return R.string.bar_chart_daily;
    }

    /**
     * Responsible for displaying the data loaded from database in view.
     * Run on UI thread.
     */
    protected void onDataLoaded(Map<Long, Long> usagePerDayMap) {
        setData(usagePerDayMap);
        scrollToEnd();
    }
}
