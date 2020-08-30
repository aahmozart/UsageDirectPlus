package godau.fynn.usagedirect.charts;

import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.wrapper.NaturalText;
import im.dacer.androidcharts.bar.Value;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WeeklyAverageBarChart extends DailyBarChart {

    @Override
    protected int getText() {
        return R.string.chart_average;
    }

    @Override
    protected void onDataLoaded(Map<Long, Long> usagePerDayMap) {
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
                values[weekday.getValue() - 1] = new Value(weekdayMap.get(weekday).average(), NaturalText.formatWeekday(weekday));
            } else {
                values[weekday.getValue() - 1] = new Value(0, NaturalText.formatWeekday(weekday));
            }
        }

        int max = Collections.max(weekdayMap.values()).average();

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        barView.setData(values);

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
