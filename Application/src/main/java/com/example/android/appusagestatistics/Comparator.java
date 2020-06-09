package com.example.android.appusagestatistics;

import android.app.usage.UsageStats;

public class Comparator {
    /**
     * The {@link java.util.Comparator} to sort a collection of {@link UsageStats} by the timestamp
     * of the last time the app was used in descending order.
     */
    public static class LastTimeLaunchedComparatorDesc implements java.util.Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
        }
    }

    /**
     * A {@link java.util.Comparator} to sort a collection of {@link UsageStats} by total screen time.
     */
    public static class TimeInForegroundComparatorDesc implements java.util.Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getTotalTimeInForeground(), left.getTotalTimeInForeground());
        }
    }
}
