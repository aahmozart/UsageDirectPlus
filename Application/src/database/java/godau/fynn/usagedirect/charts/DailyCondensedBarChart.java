package godau.fynn.usagedirect.charts;

import androidx.annotation.StringRes;
import godau.fynn.usagedirect.R;
import im.dacer.androidcharts.bar.CondensedBarView;
import im.dacer.androidcharts.bar.Value;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

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
    protected void setData(Map<Long, Long> map) {

        // Collect data and labels

        Value[] values = new Value[map.size()];

        int i = 0;
        for (Long d : map.keySet()) {
            int seconds = (int) (map.get(d) / 1000);

            LocalDate date = LocalDate.ofEpochDay(d);
            if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
                // Add label at the beginning of each week
                values[i++] = new Value(seconds, String.valueOf(date.get(WeekFields.of(Locale.getDefault()).weekOfYear())));
            } else {
                values[i++] = new Value(seconds);
            }


        }

        int max = (int) (Collections.max(map.values()) / 1000);

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        ((CondensedBarView) barView).setBarWidth(8);
        ((CondensedBarView) barView).setLabelIndicatorMode(CondensedBarView.LabelIndicatorMode.IN_CHART);
        barView.setData(values, chartMax);

    }
}
