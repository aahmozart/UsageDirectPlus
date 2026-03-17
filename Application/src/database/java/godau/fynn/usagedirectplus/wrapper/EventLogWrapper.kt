package godau.fynn.usagedirectplus.wrapper

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.content.Context
import android.util.Log
import godau.fynn.usagedirectplus.SimpleUsageStat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.OptionalLong
import java.util.function.BiConsumer

/**
 * Wrapper class for `queryEvents(...)` calls to the UsageStatsManager class
 */
open class EventLogWrapper(context: Context) : UsageStatsManagerWrapper(context) {

    private val guardian = UnmatchedCloseEventGuardian(usageStatsManager)

    init {
        val sharedPreferences = context.getSharedPreferences("timezone", Context.MODE_PRIVATE)

        // Migration to remove timezone from shared preferences
        if (sharedPreferences.contains("timezone")) {
            sharedPreferences
                .edit()
                .remove("timezone")
                .apply()
        }
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics for the specified period.
     *
     * Comments refer to the cases from
     * [the documentation.](https://codeberg.org/fynngodau/usageDirect/wiki/Event-log-wrapper-scenarios)
     *
     * @param start First point in time to include in results
     * @param end   Last point in time to include in results
     * @return A list of foreground stats for the specified period
     */
    open fun getForegroundStatsByTimestamps(start: Long, end: Long): List<ComponentForegroundStat> {
        var start = start

        /*
         * Because sometimes, open events do not have close events when they should, as a hack / workaround,
         * we query the apps currently in the foreground and match them against the apps that are currently
         * in the foreground if the query start date is very recent or in the future. Thus, we are using this
         * to tell apart True from Faulty unmatched open events.
         *
         * We query processes in the beginning of this method call in case querying the event log takes a
         * little longer.
         */
        val foregroundProcesses = ArrayList<String>()
        if (end >= System.currentTimeMillis() - 1500) {
            // Get foreground tasks
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses
            for (appProcess in appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                    appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
                ) {
                    foregroundProcesses.add(appProcess.processName)
                }
            }
        }

        // Assumption: events are ordered chronologically
        val events = usageStatsManager!!.queryEvents(start, end)

        /* ...except that sometimes, the events that are close to each other are swapped in a way that
         * breaks the assumption that all end times which do not have a matching start time have
         * started before start. We handle those as Duplicate close event and Duplicate open event.
         * Therefore, we keep null entries in our moveToForegroundMap instead of removing the entries
         * to prevent apps that had been opened previously in a period from being counted as "opened
         * before start" (as they are not a True unmatched close event).
         */

        // Map components to the last moveToForeground event
        val moveToForegroundMap = HashMap<AppClass, Long?>()

        // Collect timespans during which components are in foreground
        val componentForegroundStats = ArrayList<ComponentForegroundStat>()

        // Iterate over events
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {
                /*
                 * "An event type denoting that an android.app.Activity moved to the foreground."
                 * (old definition: "An event type denoting that a component moved to the foreground.")
                 */
                UsageEvents.Event.ACTIVITY_RESUMED,
                /*
                 * public static final int android.app.usage.UsageEvents.Event.CONTINUE_PREVIOUS_DAY = 4;
                 * (annotated as @hide)
                 * "An event type denoting that a component was in the foreground the previous day.
                 * This is effectively treated as a MOVE_TO_FOREGROUND."
                 */
                4 -> {
                    val appClass = AppClass(event.packageName, event.className ?: continue)
                    // Store open timestamp in map, overwriting earlier timestamps in case of Duplicate open event
                    moveToForegroundMap[appClass] = event.timeStamp
                }

                /*
                 * "An event type denoting that an android.app.Activity moved to the background."
                 * (old definition: "An event type denoting that a component moved to the background.")
                 */
                UsageEvents.Event.ACTIVITY_PAUSED,
                /*
                 * "An activity becomes invisible on the UI, corresponding to Activity.onStop()
                 * of the activity's lifecycle."
                 */
                UsageEvents.Event.ACTIVITY_STOPPED,
                /*
                 * public static final int android.app.usage.UsageEvents.Event.END_OF_DAY = 3;
                 * (annotated as @hide)
                 * "An event type denoting that a component was in the foreground when the stats
                 * rolled-over. This is effectively treated as a MOVE_TO_BACKGROUND."
                 */
                3 -> {
                    val appClass = AppClass(event.packageName, event.className ?: continue)
                    var eventBeginTime = moveToForegroundMap[appClass]
                    if (eventBeginTime != null) {
                        // Open and close events in order
                        moveToForegroundMap[appClass] = null
                    } else if (
                        // App has not been in this query yet (test for Duplicate close event)
                        moveToForegroundMap.keys.none { key -> event.packageName == key.packageName } &&
                        /*
                         * Test if this unmatched close event is True by asking the Guardian
                         * to scan for it
                         */
                        guardian.test(event, start)
                    ) {
                        // Identified as True unmatched close event
                        // Take start as a starting timestamp
                        eventBeginTime = start
                    } else {
                        // Ignore Faulty unmatched close event
                        continue
                    }

                    // Check if another of the app's components have moved to the foreground in the meantime
                    val endTime: OptionalLong = moveToForegroundMap.entries.stream()
                        .filter { entry -> event.packageName == entry.key.packageName }
                        .filter { entry -> entry.value != null }
                        .mapToLong { entry -> entry.value!! }
                        .min()

                    componentForegroundStats.add(
                        ComponentForegroundStat(
                            eventBeginTime,
                            endTime.orElse(event.timeStamp),
                            event.packageName
                        )
                    )
                }

                /*
                 * "An event type denoting that the Android runtime underwent a shutdown process. A
                 * DEVICE_SHUTDOWN event should be treated as if all started activities and
                 * foreground services are now stopped and no explicit ACTIVITY_STOPPED and
                 * FOREGROUND_SERVICE_STOP events will be generated for them.
                 * [... A]ny open events without matching close events between DEVICE_SHUTDOWN and
                 * DEVICE_STARTUP should be ignored because the closing time is unknown."
                 */
                UsageEvents.Event.DEVICE_SHUTDOWN -> {
                    // Per docs: iterate over remaining start events and treat them as closed
                    for (key in moveToForegroundMap.keys) {
                        if (moveToForegroundMap[key] == null) {
                            // Not a remaining start event
                            continue
                        }

                        componentForegroundStats.add(
                            ComponentForegroundStat(
                                moveToForegroundMap[key]!!,
                                event.timeStamp,
                                key.packageName
                            )
                        )

                        // Set entire app to closed
                        moveToForegroundMap.keys
                            .filter { key1 -> key.packageName == key1.packageName }
                            .forEach { samePackageKey -> moveToForegroundMap[samePackageKey] = null }
                    }
                }

                /*
                 * "An event type denoting that the Android runtime started up. This could be after
                 * a shutdown or a runtime restart. Any open events without matching close events
                 * between DEVICE_SHUTDOWN and DEVICE_STARTUP should be ignored because the
                 * closing time is unknown."
                 */
                UsageEvents.Event.DEVICE_STARTUP -> {
                    // Per docs: remove pending open events
                    for (key in moveToForegroundMap.keys) {
                        // Overwrite all times with null
                        moveToForegroundMap[key] = null
                    }

                    /* No package could be open longer than a reboot. Thus, we set the `start`
                     * timestamp to the boot event's timestamp in case we later assume that a
                     * package has been open "since the start of the period". It is not logical
                     * that this would happen but we can never know with this API.
                     */
                    start = event.timeStamp
                }
            }
        }

        // Iterate over remaining start events
        for (key in moveToForegroundMap.keys) {
            if (moveToForegroundMap[key] == null) {
                // Not a remaining start event
                continue
            }

            // Test if foreground app
            for (foregroundProcess in foregroundProcesses) {
                if (foregroundProcess.contains(key.packageName)) {
                    // Is a foreground app (True unmatched open event)
                    componentForegroundStats.add(
                        ComponentForegroundStat(
                            moveToForegroundMap[key]!!,
                            Math.min(System.currentTimeMillis(), end),
                            key.packageName
                        )
                    )
                    break
                }
            }

            // If app is not in foreground, drop event
            // Assume Faulty unmatched open event
        }

        /* If nothing happened during the timespan but there is an app in the foreground,
         * then this app was used the whole period time and there was No event for it.
         * Because the foreground applications API call is documented as not to be used
         * for purposes like this, we first query whether the process name is a valid
         * package name and if not, we drop it.
         */
        if (moveToForegroundMap.keys.isEmpty()) {
            val packageManager = context.packageManager
            for (foregroundProcess in foregroundProcesses) {
                if (packageManager.getLaunchIntentForPackage(foregroundProcess) != null) {
                    componentForegroundStats.add(
                        ComponentForegroundStat(
                            start,
                            Math.min(System.currentTimeMillis(), end),
                            foregroundProcess
                        )
                    )
                    Log.d(
                        "EventLogWrapper",
                        "Assuming that application $foregroundProcess has been used the whole query time"
                    )
                }
            }
        }

        return componentForegroundStats
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics for the specified relative day.
     *
     * @param offset Day to query back in time relative to today
     */
    fun getForegroundStatsByRelativeDay(offset: Int): List<ComponentForegroundStat> {
        // Calculate timespan to query
        val queryDay = LocalDate.now().minusDays(offset.toLong())

        val beginTime = queryDay
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val endTime = queryDay
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return getForegroundStatsByTimestamps(beginTime, endTime)
    }

    /**
     * Collects event information from system to calculate and aggregate precise
     * foreground time statistics starting at `start` and ending at
     * the end of the day that contains `start`.
     *
     * @param start Starting time of query and point in time in day to query
     */
    fun getForegroundStatsByPartialDay(start: Long): List<ComponentForegroundStat> {
        val zone = ZoneId.systemDefault()
        val endTime = Instant.ofEpochMilli(start)
            .atZone(zone)
            .toLocalDate() // remove time (and zone) information
            .plusDays(1) // go one day ahead
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return getForegroundStatsByTimestamps(start, endTime)
    }

    /**
     * Takes a list of foreground stats and aggregates them to usage stats.
     *
     * Assumes all provided usage stats to be on the same day.
     */
    fun aggregateForegroundStats(foregroundStats: List<ComponentForegroundStat>): List<SimpleUsageStat> {
        return aggregateForegroundStats(foregroundStats, null)
    }

    /**
     * Takes a list of foreground stats and aggregates them to usage stats.
     *
     * Assumes all provided usage stats to be on the same day.
     *
     * @param endConsumer Consumer that accepts ending times of component
     *                    foreground stats with their package name
     */
    fun aggregateForegroundStats(
        foregroundStats: List<ComponentForegroundStat>,
        endConsumer: BiConsumer<String, Long>?
    ): List<SimpleUsageStat> {
        val usageStats = ArrayList<SimpleUsageStat>()

        if (foregroundStats.isEmpty()) {
            return usageStats
        }

        val applicationTotalForegroundTime = HashMap<String, Long>()

        for (foregroundStat in foregroundStats) {
            if (applicationTotalForegroundTime.containsKey(foregroundStat.packageName)) {
                val newTotal = applicationTotalForegroundTime[foregroundStat.packageName]!! +
                    (foregroundStat.endTime - foregroundStat.beginTime)
                applicationTotalForegroundTime[foregroundStat.packageName] = newTotal
            } else {
                applicationTotalForegroundTime[foregroundStat.packageName] =
                    foregroundStat.endTime - foregroundStat.beginTime
            }

            endConsumer?.accept(foregroundStat.packageName, foregroundStat.endTime)
        }

        val day = Instant.ofEpochMilli(foregroundStats[0].beginTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()

        for (application in applicationTotalForegroundTime.keys) {
            usageStats.add(
                SimpleUsageStat(day, applicationTotalForegroundTime[application]!!, application)
            )
        }

        return usageStats
    }

    /**
     * Collects **all** event information from system to calculate and aggregate precise
     * foreground time statistics for the provided day and presents this information as
     * [SimpleUsageStat]s.
     *
     * @param day Day since epoch
     */
    fun getForegroundStatsByDay(day: Long): List<ComponentForegroundStat> {
        val date = LocalDate.ofEpochDay(day)
        val start = date.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val end = date.plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        return getForegroundStatsByTimestamps(start, end)
    }

    /**
     * Collects **all** event information from system to calculate and aggregate precise
     * foreground time statistics and presents this information as [SimpleUsageStat]s.
     *
     * **This method call causes lag** if called with a low since value.
     *
     * @param daySince Return data from this day on
     * @param endConsumer Consumer that accepts ending times of component
     *                    foreground stats with their package name
     */
    fun getAllSimpleUsageStats(daySince: Long, endConsumer: BiConsumer<String, Long>): List<SimpleUsageStat> {
        val usageStats = ArrayList<SimpleUsageStat>()

        val today = LocalDate.now().toEpochDay()

        // Maximum event log size
        var currentDay = Math.max(today - 10, daySince)

        while (currentDay <= today) {
            val foregroundStats = getForegroundStatsByDay(currentDay)
            usageStats.addAll(aggregateForegroundStats(foregroundStats, endConsumer))
            currentDay++
        }

        return usageStats
    }

    /**
     * Returns only usage statistics that have not been counted yet for
     * only the day that contains `timestamp`
     *
     * @param endConsumer Consumer that accepts ending times of component
     *                    foreground stats with their package name
     */
    fun getIncrementalSimpleUsageStats(
        timestamp: Long,
        endConsumer: BiConsumer<String, Long>
    ): List<SimpleUsageStat> {
        val foregroundStats = getForegroundStatsByPartialDay(timestamp)
        return aggregateForegroundStats(foregroundStats, endConsumer)
    }

    /**
     * Stores a class name and its corresponding package.
     */
    private data class AppClass(
        val packageName: String,
        val className: String
    )
}
