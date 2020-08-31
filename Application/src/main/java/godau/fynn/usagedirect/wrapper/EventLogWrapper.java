package godau.fynn.usagedirect.wrapper;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/**
 * Wrapper class for <code>queryEvents(…)</code> calls to the UsageStatsManager class
 */
public class EventLogWrapper extends UsageStatsManagerWrapper {

    private TimeZone timezone;

    public EventLogWrapper(Context context) {
        super(context);

        SharedPreferences sharedPreferences = context.getSharedPreferences("timezone", Context.MODE_PRIVATE);

        if (sharedPreferences.contains("timezone")) {
            timezone =
                    TimeZone.getTimeZone(
                            sharedPreferences.getString("timezone", null)
                    );
        } else {
            resetTimezone();
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
        // Assumption: events are ordered chronologically
        UsageEvents events = usageStatsManager.queryEvents(start, end);

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
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics for the specified relative day.
     *
     * @param offset Day to query back in time relative to today
     */
    public List<ComponentForegroundStat> getForegroundStatsByRelativeDay(int offset) {

        // Calculate timespan to query

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeZone(timezone);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Calendar might have moved to a different day when setting the timezone
        Calendar reference = Calendar.getInstance();
        calendar.set(Calendar.YEAR, reference.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, reference.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, reference.get(Calendar.DAY_OF_MONTH));

        calendar.add(Calendar.DAY_OF_MONTH, -offset);

        long beginTime = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endTime = calendar.getTimeInMillis();

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
        long endTime = Instant.ofEpochMilli(start)
                .atZone(ZoneId.of(timezone.getID()))
                .toLocalDate() // remove time (and zone) information
                .plusDays(1) // go one day ahead
                .atStartOfDay(ZoneId.of(timezone.getID()))
                .toInstant()
                .toEpochMilli();

        return getForegroundStatsByTimestamps(start, endTime);
    }

    /**
     * Takes a list of foreground stats and aggregates them to usage stats.
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
                .atZone(ZoneId.of(timezone.getID()))
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
     * foreground time statistics and presents this information as {@link SimpleUsageStat}s.
     * <p><b>This method call causes lag</b> if called with a low since value.
     *
     * @param daySince Return data from this day onwards (respects {@link #timezone})
     */
    public List<SimpleUsageStat> getAllSimpleUsageStats(long daySince) { // TODO
        List<ComponentForegroundStat> foregroundStats;
        int relativeDay;

        foregroundStats = getForegroundStatsByRelativeDay(relativeDay = 0);

        List<SimpleUsageStat> usageStats = new ArrayList<>();

        while (foregroundStats.size() > 0) {

            List<SimpleUsageStat> newUsageStats = aggregateForegroundStats(foregroundStats);

            usageStats.addAll(newUsageStats);

            if (newUsageStats.get(0).getDay() <= daySince) {
                // Reached first day that should be returned by this query
                break;
            }

            foregroundStats = getForegroundStatsByRelativeDay(++relativeDay);
        }

        Log.d("USW", "Returning data for up to day -" + relativeDay + " (" + usageStats.size() + " entries)");
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

    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Sets time zone to current system time zone and persists this value
     * in shared preferences.
     */
    private void resetTimezone() {
        timezone = TimeZone.getDefault();
        context.getSharedPreferences("timezone", Context.MODE_PRIVATE)
                .edit().putString("timezone", timezone.getID()).apply();
    }
}
