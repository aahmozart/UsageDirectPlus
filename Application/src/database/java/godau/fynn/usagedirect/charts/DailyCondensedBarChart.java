package godau.fynn.usagedirect.charts;

import androidx.annotation.StringRes;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.ColoredSimpleUsageStat;
import im.dacer.androidcharts.bar.CondensedBarView;
import im.dacer.androidcharts.bar.MultiValue;
import im.dacer.androidcharts.bar.Value;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;

public class DailyCondensedBarChart extends DailyBarChart {

    @Override
    protected int getLayout() {
        return R.layout.content_bar_view_condensed;
    }

    @Override
    protected @StringRes
    int getText() {
        return R.string.charts_bar_daily_condensed;
    }

    @Override
    protected void setData(long[] days, ColoredSimpleUsageStat[] coloredUsageStats) {

        // Collect data and labels
        Value[] values = new Value[days.length];

        WeekFields week = WeekFields.ISO;

        int max = 0, i = 0;
        for (Long d : days) {
            ArrayList<Integer> seconds = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // Gather usage stats for this day
            for (ColoredSimpleUsageStat coloredSimpleUsageStat : coloredUsageStats) {
                if (coloredSimpleUsageStat.getDay() != d) continue;

                seconds.add((int) (coloredSimpleUsageStat.getTimeUsed() / 1000));
                colors.add(coloredSimpleUsageStat.getColor());
            }

            LocalDate date = LocalDate.ofEpochDay(d);

            values[i++] = new MultiValue(
                    seconds.stream().mapToInt(Integer::intValue).toArray(),
                    colors.toArray(new Integer[0]),
                    // Add label at the beginning of each week
                    date.getDayOfWeek() == week.getFirstDayOfWeek()?
                    String.valueOf(date.get(week.weekOfYear())) : null
            );

            int dayTotal = seconds.stream().mapToInt(Integer::intValue).sum();
            if (dayTotal > max) max = dayTotal;
        }

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        ((CondensedBarView) barView).setBarWidth(8);
        ((CondensedBarView) barView).setLabelIndicatorMode(CondensedBarView.LabelIndicatorMode.IN_CHART);
        barView.setData(values, chartMax);

        barView.setZeroLineEnabled(true);
    }
}
