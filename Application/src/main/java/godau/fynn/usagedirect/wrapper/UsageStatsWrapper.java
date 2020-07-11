/*
 * usageDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package godau.fynn.usagedirect.wrapper;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.*;

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
    public List<UsageStats> getUsageStatistics(Interval interval, int offset) {

        long endTime = interval.backInTime(offset).getTimeInMillis();
        long beginTime = endTime - 60000;

        return usageStatsManager.queryUsageStats(interval.interval, beginTime, endTime);
    }

    /**
     * Accumulate UsageStatistics of a day
     * @see #getUsageStatistics(Interval, int)
     * @return A time value in seconds
     */
    public int getAccumulatedTime(Interval interval, int offset) {

        List<UsageStats> usageStats = getUsageStatistics(interval, offset);

        int sum = 0;

        for (UsageStats stats : usageStats) {
            sum += stats.getTotalTimeInForeground() / 1000;
        }

        return sum;
    }

    /**
     * Accumulates UsageStatistics of multiple days
     * @param intervals How many intervals back in time should be added to the list
     * @return A chronologically ordered list of time values in seconds, containing
     *         <code>intervals + 1</code> items
     */
    public ArrayList<Integer> getAccumulatedTimes(Interval interval, int intervals) {
        ArrayList<Integer> accumulation = new ArrayList<>();

        for (int i = intervals; i >= 0; i--) {
            accumulation.add(getAccumulatedTime(interval, i));
        }

        return accumulation;
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics.
     *
     * @param offset Day to query back in time relative to today
     */
    public List<ComponentForegroundStat> getForegroundStatsByRelativeDay(int offset) {

        // Calculate timespan to query

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, -offset);

        long beginTime = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endTime = calendar.getTimeInMillis();

        // Assumption: events are ordered chronologically
        UsageEvents events = usageStatsManager.queryEvents(beginTime, endTime);

        // Map package names to the last moveToForeground event
        Map<String, Long> moveToForegroundMap = new HashMap<>();

        // Collect timespans during which components are in foreground
        ArrayList<ComponentForegroundStat> componentForegroundStats = new ArrayList<>();

        // Iterate over events
        UsageEvents.Event event = new UsageEvents.Event();

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            switch (event.getEventType()) {
                /*
                 * An event type denoting that a component moved to the foreground.
                 */
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                /*
                 * public static final int android.app.usage.UsageEvents.Event.CONTINUE_PREVIOUS_DAY = 4;
                 * Copy of documentation:
                 * "An event type denoting that a component was in the foreground the previous day.
                 * This is effectively treated as a MOVE_TO_FOREGROUND."
                 */
                case 4:
                    moveToForegroundMap.put(event.getPackageName(), event.getTimeStamp());

                    break;
                /*
                 * "An event type denoting that a component moved to the background."
                 */
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                /*
                 * public static final int android.app.usage.UsageEvents.Event.END_OF_DAY = 3;
                 * Copy of documentation:
                 * "An event type denoting that a component was in the foreground when the stats
                 * rolled-over. This is effectively treated as a {@link #MOVE_TO_BACKGROUND}."
                 */
                case 3:
                    if (moveToForegroundMap.containsKey(event.getPackageName())) {
                        long eventBeginTime = moveToForegroundMap.get(event.getPackageName());
                        moveToForegroundMap.remove(event.getPackageName());

                        componentForegroundStats.add(new ComponentForegroundStat(
                                eventBeginTime,
                                event.getTimeStamp(),
                                event.getPackageName()
                        ));
                    }
                    break;

            }
        }

        return componentForegroundStats;
    }

    /**
     * Incrementally tests intervals further in the past to find out the total amount
     * of intervals that have data associated with them.
     * <p>Take care, this method has <b>bad performance</b>.
     *
     * @return Amount of intervals with a corresponding dataset
     */
    public int getDatasetAmount(Interval interval) {
        List<UsageStats> stats;
        int amount = 0;
        do {
            stats = getUsageStatistics(interval, amount++);
        } while (stats.size() > 0);
        return --amount;
    }

    /**
     * Tests whether usage stats permission has been granted by the user.
     * If not, user needs to be prompted to grant permission in settings.
     *
     * @see <a href="https://stackoverflow.com/a/28921586">StackOverflow</a>
     */
    public boolean isPermissionGranted() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
