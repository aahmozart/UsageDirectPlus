package godau.fynn.usagedirect.charts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.HistoryDatabase;

import java.util.Map;

public class DailyBarChart extends UsageStatBarChart {

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {

        setText(getText());

        new Thread(() -> {

            HistoryDatabase database = HistoryDatabase.get(getContext());

            final Map<Long, Long> usagePerDayMap = database.getUsageStatsDao().getTotalTimePerDay();

            database.close();

            new Handler(Looper.getMainLooper()).post(() ->
                    onDataLoaded(usagePerDayMap)
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
    protected void onDataLoaded(Map<Long, Long> usagePerDayMap) {
        setData(usagePerDayMap);
        scrollToEnd();
    }
}
