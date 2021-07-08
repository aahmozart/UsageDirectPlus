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
import im.dacer.androidcharts.bar.MultiValue;
import im.dacer.androidcharts.bar.Value;

import java.time.LocalDate;
import java.util.ArrayList;

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

    /**
     * The label is gathered from {@link #getLabel(LocalDate)}.
     */
    @Override
    protected void setData(long[] days, ColoredSimpleUsageStat[] coloredUsageStats) {
        // Collect data and labels

        Value[] values = new Value[days.length];

        int i = 0, max = 0;
        for (Long d : days) {

            ArrayList<Integer> seconds = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            int uncoloredSeconds = 0;

            // Gather usage stats for this day
            for (ColoredSimpleUsageStat coloredSimpleUsageStat : coloredUsageStats) {
                if (coloredSimpleUsageStat.getDay() != d) continue;

                if (coloredSimpleUsageStat.getColor() == null) {
                    uncoloredSeconds += coloredSimpleUsageStat.getTimeUsed() / 1000;
                    continue;
                }

                seconds.add((int) (coloredSimpleUsageStat.getTimeUsed() / 1000));
                colors.add(coloredSimpleUsageStat.getColor());
            }

            seconds.add(uncoloredSeconds);
            colors.add(null);

            LocalDate date = LocalDate.ofEpochDay(d);

            values[i++] = new MultiValue(
                    seconds.stream().mapToInt(Integer::intValue).toArray(),
                    colors.toArray(new Integer[0]),
                    getLabel(date)
            );

            int dayTotal = seconds.stream().mapToInt(Integer::intValue).sum();
            if (dayTotal > max) max = dayTotal;
        }

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        barView.setData(values, chartMax);

        // Kinda hacky – we want to avoid an additional method call
        // Don't add scale for subclasses
        if (this.getClass().equals(DailyBarChart.class)) {
            addScale(chartMax);
        }
    }

    /**
     * This method call should be overwritten by subclasses and determines the label
     * that a specific <code>date</code> should be shown with in the chart.
     *
     * @param date Date for which a label must be generated
     * @return <code>null</code> in case of no label
     */
    protected String getLabel(LocalDate date) {
        return String.valueOf(date.getDayOfMonth());
    }
}
