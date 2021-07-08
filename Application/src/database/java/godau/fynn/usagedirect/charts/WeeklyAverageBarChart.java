package godau.fynn.usagedirect.charts;

import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.ColoredSimpleUsageStat;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.wrapper.TextFormat;
import im.dacer.androidcharts.bar.Value;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WeeklyAverageBarChart extends UsageStatBarChart {

    private Map<Long, Long> usagePerDayMap;

    @Override
    protected int getText() {
        return R.string.chart_average;
    }

    @Override
    protected void getData(HistoryDatabase database) {
        usagePerDayMap = database.getUsageStatsDao().getTotalTimePerDay();
    }

    @Override
    protected void onDataLoaded() {
        Map<DayOfWeek, Average> weekdayMap = new HashMap<>();

        for (Long d : usagePerDayMap.keySet()) {
            int seconds = (int) (usagePerDayMap.get(d) / 1000);

            Average a;
            DayOfWeek weekday = LocalDate.ofEpochDay(d).getDayOfWeek();
            if (weekdayMap.containsKey(weekday)) {
                a = weekdayMap.get(weekday);
            } else {
                weekdayMap.put(weekday, a = new Average());
            }

            a.add(seconds);
        }

        Value[] values = new Value[7];
        // weekday contains the values 2 (MONDAY) to 7 (SATURDAY), then 1 (SUNDAY)
        for (DayOfWeek weekday : DayOfWeek.values()) {

            if (weekdayMap.containsKey(weekday)) {
                values[weekday.getValue() - 1] = new Value(weekdayMap.get(weekday).average(), TextFormat.formatWeekday(weekday));
            } else {
                values[weekday.getValue() - 1] = new Value(0, TextFormat.formatWeekday(weekday));
            }
        }

        int max = Collections.max(weekdayMap.values()).average();

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        barView.setData(values, chartMax);

        addScale(chartMax);

    }

    private static class Average implements Comparable<Average> {
        private int count;
        private int sum;

        public void add(int value) {
            count++;
            sum += value;
        }

        public int average() {
            return sum / count;
        }

        @Override
        public int compareTo(Average o) {
            return Integer.compare(average(), o.average());
        }
    }
}
