package godau.fynn.usagedirect.charts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.ColoredSimpleUsageStat;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.UsageStatsDao;

public class DailyBarChart extends UsageStatBarChart {

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {

        setText(getText());

        new Thread(() -> {

            HistoryDatabase database = HistoryDatabase.get(getContext());

            UsageStatsDao usageStatsDao = database.getUsageStatsDao();

            long minDay = usageStatsDao.getMinimumDay();
            long maxDay = usageStatsDao.getMaximumDay();

            final long[] displayDays = new long[(int) (maxDay - minDay) + 1];
            int i = 0;
            for (long day = minDay; day <= maxDay; i++, day++) {
                displayDays[i] = day;
            }

            final ColoredSimpleUsageStat[] coloredUsageStats = database.getAppColorDao().getColoredUsageStats();

            database.close();

            new Handler(Looper.getMainLooper()).post(() ->
                    onDataLoaded(displayDays, coloredUsageStats)
            );
        }).start();
    }

    protected @StringRes
    int getText() {
        return R.string.charts_bar_daily;
    }

    /**
     * Responsible for displaying the data loaded from database in view.
     * Run on UI thread.
     */
    protected void onDataLoaded(long[] displayDays,
                                ColoredSimpleUsageStat[] coloredUsageStats) {
        setData(displayDays, coloredUsageStats);
        scrollToEnd();
    }
}
