package godau.fynn.usagedirect.charts;

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

    private Value[] values;
    private int chartMax;

    protected @StringRes
    int getText() {
        return R.string.charts_bar_daily;
    }

    /**
     * Queries data that is then used for the chart. Furthermore calculates
     * data for the bar view for each of the days in the <code>days</code>
     * array, in its order.
     * <p>
     * The label is gathered from {@link #getLabel(LocalDate)}.
     */
    @Override
    protected void getData(HistoryDatabase database) {
        UsageStatsDao usageStatsDao = database.getUsageStatsDao();

        long minDay = usageStatsDao.getMinimumDay();
        long maxDay = usageStatsDao.getMaximumDay();

        long[] displayDays = new long[(int) (maxDay - minDay) + 1];
        int i = 0;
        for (long day = minDay; day <= maxDay; i++, day++) {
            displayDays[i] = day;
        }

        ColoredSimpleUsageStat[] coloredUsageStats = database.getAppColorDao().getColoredUsageStats();

        // Collect data and labels

        values = new Value[displayDays.length];

        i = 0;
        int max = 0;
        for (Long d : displayDays) {

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
        chartMax = max + (60 * 30);

    }

    /**
     * Displays the data that is calculated in {@link #getData(HistoryDatabase)}
     * in the chart view.
     */
    protected void onDataLoaded() {

        barView.setData(values, chartMax);

        scrollToEnd();

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
