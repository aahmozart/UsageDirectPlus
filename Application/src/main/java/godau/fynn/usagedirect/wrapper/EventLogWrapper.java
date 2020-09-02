package godau.fynn.usagedirect.wrapper;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.SharedPreferences;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class for <code>queryEvents(…)</code> calls to the UsageStatsManager class
 */
public class EventLogWrapper extends UsageStatsManagerWrapper {

    public EventLogWrapper(Context context) {
        super(context);

        SharedPreferences sharedPreferences = context.getSharedPreferences("timezone", Context.MODE_PRIVATE);

        // Migration to remove timezone from shared preferences
        if (sharedPreferences.contains("timezone")) {
            sharedPreferences
                    .edit()
                    .remove("timezone")
                    .apply();
        }

    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics for the specified period.
     *
     * @param start First point in time to include in results
     * @param end   Last point in time to include in results
     * @return A list of foreground stats for the specified period
     */
    public List<ComponentForegroundStat> getForegroundStatsByTimestamps(long start, long end) {

        /*
         * Because sometimes, open events do not have close events when they should, as a hack / workaround,
         * we query the apps currently in the foreground and match them against the apps that are currently
         * in the foreground if the query start date is very recent or in the future.
         *
         * We query processes in the beginning of this method call in case querying the event log takes a
         * little longer.
         */
        List<String> foregroundProcesses = new ArrayList<>();
        if (end >= System.currentTimeMillis() - 1500) {

            // Get foreground tasks
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    foregroundProcesses.add(appProcess.processName);
                }
            }
        }


        // Assumption: events are ordered chronologically
        UsageEvents events = usageStatsManager.queryEvents(start, end);

        /* …except that sometimes, the events that are close to each other are swapped in a way that
         * breaks the assumption that all end times which do not have a matching start time have
         * started before start. Therefore, we keep null entries in our moveToForegroundMap instead
         * of removing the entries to prevent apps that had been opened previously in a period from
         * being counted as "opened before start".
        */

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
                    long eventBeginTime;
                    if (moveToForegroundMap.get(event.getPackageName()) != null) {
                        eventBeginTime = moveToForegroundMap.get(event.getPackageName());
                        moveToForegroundMap.put(event.getPackageName(), null);
                    } else if (!moveToForegroundMap.containsKey(event.getPackageName())) {
                        // App has been launched before start, take start as a starting timestamp
                        eventBeginTime = start;
                    } else break;

                    componentForegroundStats.add(new ComponentForegroundStat(
                            eventBeginTime,
                            event.getTimeStamp(),
                            event.getPackageName()
                    ));
                    break;

            }
        }

        // Iterate over remaining start events
        for (String packageName : moveToForegroundMap.keySet()) {

            if (moveToForegroundMap.get(packageName) == null) {
                // Not a remaining start event
                continue;
            }

            // Test if foreground app
            for (String foregroundProcess : foregroundProcesses) {
                if (foregroundProcess.contains(packageName)) {

                    // Is a foreground app
                    componentForegroundStats.add(new ComponentForegroundStat(
                            moveToForegroundMap.get(packageName),
                            Math.min(System.currentTimeMillis(), end),
                            packageName
                    ));

                    break;
                }
            }

            // If app is not in foreground, drop event
        }

        return componentForegroundStats;
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics for the specified relative day.
     *
     * @param offset Day to query back in time relative to today
     */
    public List<ComponentForegroundStat> getForegroundStatsByRelativeDay(int offset) {

        // Calculate timespan to query
        LocalDate queryDay = LocalDate.now()
                .minusDays(offset);

        long beginTime = queryDay
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        long endTime = queryDay
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return getForegroundStatsByTimestamps(beginTime, endTime);
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics starting at <code>start</code> and ending at
     * the end of the day that contains <code>start</code>.
     *
     * @param start Starting time of query and point in time in day to query
     */
    public List<ComponentForegroundStat> getForegroundStatsByPartialDay(long start) {
        ZoneId zone = ZoneId.systemDefault();
        long endTime = Instant.ofEpochMilli(start)
                .atZone(zone)
                .toLocalDate() // remove time (and zone) information
                .plusDays(1) // go one day ahead
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli();

        return getForegroundStatsByTimestamps(start, endTime);
    }

    /**
     * Takes a list of foreground stats and aggregates them to usage stats.
     * <p>Assumes all provided usage stats to be on the same day.</p>
     */
    public List<SimpleUsageStat> aggregateForegroundStats(List<ComponentForegroundStat> foregroundStats) {

        List<SimpleUsageStat> usageStats = new ArrayList<>();

        if (foregroundStats.size() == 0) {
            return usageStats;
        }

        Map<String, Long> applicationTotalForegroundTime = new HashMap<>();

        for (ComponentForegroundStat foregroundStat : foregroundStats) {
            if (applicationTotalForegroundTime.containsKey(foregroundStat.packageName)) {

                long newTotal = applicationTotalForegroundTime.get(foregroundStat.packageName)
                        + (foregroundStat.endTime - foregroundStat.beginTime);

                applicationTotalForegroundTime.put(foregroundStat.packageName, newTotal);

            } else {

                applicationTotalForegroundTime.put(foregroundStat.packageName,
                        (foregroundStat.endTime - foregroundStat.beginTime)
                );

            }
        }

        long day = Instant.ofEpochMilli(foregroundStats.get(0).beginTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay();

        for (String application : applicationTotalForegroundTime.keySet()) {
            usageStats.add(
                    new SimpleUsageStat(day, applicationTotalForegroundTime.get(application), application)
            );
        }

        return usageStats;

    }

    /**
     * Collects <b>all</b> event information from system to calculate and aggregate precise
     * foreground time statistics for the provided day and presents this information as
     * {@link SimpleUsageStat}s.
     *
     * @param day Day since epoch
     */
    private List<ComponentForegroundStat> getForegroundStatsByDay(long day) {
        LocalDate date = LocalDate.ofEpochDay(day);
        long start = date.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long end = date.plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        return getForegroundStatsByTimestamps(start, end);
    }

    /**
     * Collects <b>all</b> event information from system to calculate and aggregate precise
     * foreground time statistics and presents this information as {@link SimpleUsageStat}s.
     * <p><b>This method call causes lag</b> if called with a low since value.
     *
     * @param daySince Return data from this day onwards
     */
    public List<SimpleUsageStat> getAllSimpleUsageStats(long daySince) {
        List<SimpleUsageStat> usageStats = new ArrayList<>();
        List<ComponentForegroundStat> foregroundStats;

        long today = LocalDate.now().toEpochDay();

        // Maximum event log size
        daySince = Math.max(today - 10, daySince);

        while (daySince <= today) {
            foregroundStats = getForegroundStatsByDay(daySince);

            usageStats.addAll(
                    aggregateForegroundStats(foregroundStats)
            );

            daySince++;
        }

        return usageStats;
    }

    /**
     * Returns only usage statistics that have not been counted yet for
     * only the day that contains <code>timestamp</code>
     */
    public List<SimpleUsageStat> getIncrementalSimpleUsageStats(long timestamp) {

        List<ComponentForegroundStat> foregroundStats = getForegroundStatsByPartialDay(timestamp);
        return aggregateForegroundStats(foregroundStats);
    }
}
