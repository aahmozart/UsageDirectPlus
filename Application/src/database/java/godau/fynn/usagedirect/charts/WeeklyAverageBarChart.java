package godau.fynn.usagedirect.charts;

import androidx.annotation.ColorInt;
import androidx.room.*;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.persistence.AppColor;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.wrapper.TextFormat;
import im.dacer.androidcharts.bar.MultiValue;
import im.dacer.androidcharts.bar.Value;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeeklyAverageBarChart extends UsageStatBarChart {

    private Value[] usagePerDayMap;

    @Override
    protected int getText() {
        return R.string.chart_average;
    }

    @Override
    protected void getData(HistoryDatabase database) {
        usagePerDayMap = database.getWeeklyDao().getData();
    }

    @Override
    protected void onDataLoaded() {

        int max = Arrays.stream(usagePerDayMap)
                .mapToInt(v -> v.getValue())
                .max().getAsInt();

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        barView.setData(usagePerDayMap, chartMax);

        addScale(chartMax);

    }

    public static class UsageDay {
        long day;
        @Relation(
                parentColumn = "day",
                entityColumn = "day",
                entity = SimpleUsageStat.class
        )
        List<SimpleUsageStat> usageStats;
    }

    @Dao
    public abstract static class WeeklyAverageDao {

        @Query(
                "SELECT day, timeUsed, usageStats.applicationId AS applicationId FROM usageStats " +
                        "LEFT JOIN colors ON usageStats.applicationId == colors.applicationId " +
                        "WHERE hidden = 0 " +
                        "GROUP BY day"
        )
        protected abstract UsageDay[] getUsageDays();

        @Query(
                "SELECT DISTINCT colors.applicationId, color, priority FROM colors " +
                        // Only return colors that actually appear in the usageStats to avoid NullPointerExceptions
                        // in connection with the Map that is filled using `getAllApplicationIds()`
                        "INNER JOIN usageStats ON usageStats.applicationId == colors.applicationId " +
                        "ORDER BY priority DESC"
        )
        protected abstract AppColor[] getColors();

        @Query("SELECT DISTINCT applicationId FROM usageStats")
        protected abstract String[] getAllApplicationIds();


        @Transaction
        public Value[] getData() {

            Value[] values = new Value[DayOfWeek.values().length];

            // Query usage data for all days (grouped by day in UsageDay object)
            UsageDay[] usageDays = getUsageDays();
            String[] allApplicationIds = getAllApplicationIds();

            for (int day = 0; day < DayOfWeek.values().length; day++) {
                DayOfWeek weekday = DayOfWeek.values()[day];

                // Prepare sum map by setting each existing package name to zero
                Map<String, Long> applicationSum = new HashMap<>();
                for (String applicationId : allApplicationIds) {
                    applicationSum.put(applicationId, 0L);
                }

                // Count days for average calculation
                int daysConsidered = 0;
                for (UsageDay usageDay : usageDays) {

                    // Skip days that are not of the correct weekday
                    if (LocalDate.ofEpochDay(usageDay.day).getDayOfWeek() != weekday) continue;
                    else daysConsidered++;

                    for (SimpleUsageStat stat : usageDay.usageStats) {

                        applicationSum.put(stat.getApplicationId(),
                                applicationSum.get(stat.getApplicationId())
                                        + stat.getTimeUsed()
                        );
                    }
                }

                // Pull values for colored apps
                AppColor[] colors = getColors();

                @ColorInt Integer[] colorInts = new Integer[colors.length + 1];
                int[] averageTimes = new int[colors.length + 1];

                for (int i = 0; i < colors.length; i++) {
                    AppColor color = colors[i];

                    colorInts[i] = color.getColor();

                    if (daysConsidered > 0) {
                        averageTimes[i] = (int) (applicationSum.get(color.getApplicationId()) / 1000 / daysConsidered);
                    } else {
                        averageTimes[i] = 0;
                    }

                    // Remove from map
                    applicationSum.remove(color.getApplicationId());
                }

                // Add values for uncolored apps (all remaining values in map)
                colorInts[colors.length] = null;
                int finalDaysConsidered = daysConsidered;
                if (finalDaysConsidered > 0) {
                    averageTimes[colors.length] = applicationSum.values()
                            .stream()
                            .mapToInt(l -> (int) (l / 1000 / finalDaysConsidered))
                            .sum();
                } else {
                    averageTimes[colors.length] = 0;
                }

                // Construct MultiValue for weekday
                values[day] =
                        new MultiValue(
                                averageTimes, colorInts, TextFormat.formatWeekday(weekday)
                        );
            }

            return values;

        }
    }
}
