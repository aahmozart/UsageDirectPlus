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
import android.content.SharedPreferences;
import android.util.Log;

import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper class for <code>queryUsageStats(…)</code> calls to the UsageStatsManager class
 * <p>Uses a <code>static</code> cache to speed up requests. If you want to clear the cache,
 * call {@link #flushCache()}.
 */
public class UsageStatsWrapper extends UsageStatsManagerWrapper {

    private static Map<Long, List<UsageStats>> cache = new ConcurrentHashMap<>();

    public UsageStatsWrapper(Context context) {
        super(context);
    }

    /**
     * Clears cache
     */
    public static void flushCache() {
        cache = new ConcurrentHashMap<>();
    }

    /**
     * <p>Assumes usage stats permission is granted, check beforehand using
     * {@link #isPermissionGranted()}.
     * <p>Uses a local cache to be speed up requests and thus does not guarantee
     * live data
     *
     * @param interval The time interval by which the stats are aggregated.
     * @param offset   Amount of intervals to go back in time
     * @return A list of {@link android.app.usage.UsageStats}.
     */
    public List<UsageStats> getUsageStatistics(Interval interval, int offset) {

        long hash = Objects.hash(interval, offset);
        if (cache.containsKey(hash)) {
            return cache.get(hash);
        } else {
            long endTime = interval.backInTime(offset).getTimeInMillis();
            long beginTime = endTime - 60000;

            List<UsageStats> usageStats = usageStatsManager.queryUsageStats(interval.interval, beginTime, endTime);
            cache.put(hash, usageStats);
            return usageStats;
        }
    }

    /**
     * Accumulate UsageStatistics of a period
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

}
