package com.example.android.appusagestatistics;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

/**
 * Wrapper class for UsageStatsManager class
 */
public class UsageStatsWrapper {

    private final Context context;
    private final UsageStatsManager usageStatsManager;

    @SuppressLint("WrongConstant")
    public UsageStatsWrapper(Context context) {
        this.context = context;
        usageStatsManager = (UsageStatsManager) context
                .getSystemService("usagestats"); //Context.USAGE_STATS_SERVICE
    }

    /**
     * <p>Assumes usage stats permission is granted, check beforehand using
     * {@link #isPermissionGranted()}.
     *
     * @param interval The time interval by which the stats are aggregated.
     * @param offset   Amount of intervals to go back in time
     * @return A list of {@link android.app.usage.UsageStats}.
     */
    public List<UsageStats> getUsageStatistics(StatsUsageInterval interval, int offset) {

        long endTime = interval.backInTime(offset).getTimeInMillis();
        long beginTime = endTime - 60000;

        return usageStatsManager.queryUsageStats(interval.interval, beginTime, endTime);
    }

    /**
     * Tests whether usage stats permission has been granted by the user.
     * If not, user needs to be prompted to grant permission in settings.
     *
     * @see <a href="https://stackoverflow.com/a/28921586">StackOverflow</a>
     */
    boolean isPermissionGranted() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * Enum represents the intervals for {@link android.app.usage.UsageStatsManager} so that
     * values for intervals can be found by a String representation. Furthermore calculates
     * a timepoint somewhen in a past interval.
     */
    enum StatsUsageInterval {

        DAILY(UsageStatsManager.INTERVAL_DAILY, Calendar.DAY_OF_MONTH),
        WEEKLY(UsageStatsManager.INTERVAL_WEEKLY, Calendar.WEEK_OF_MONTH),
        MONTHLY(UsageStatsManager.INTERVAL_MONTHLY, Calendar.MONTH),
        YEARLY(UsageStatsManager.INTERVAL_YEARLY, Calendar.YEAR);


        private final int interval;
        private final int calendarField;

        /**
         * @param interval             {@link UsageStatsManager} interval
         * @param calendarField        Duration of the interval in milliseconds
         */
        StatsUsageInterval(int interval, int calendarField) {
            this.interval = interval;
            this.calendarField = calendarField;
        }

        /**
         * @param times Amount of intervals to go back
         * @return A calendar that lies within the <code>times</code>-th interval back in time
         */
        public Calendar backInTime(int times) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(calendarField, -times);
            return calendar;
        }
    }
}
