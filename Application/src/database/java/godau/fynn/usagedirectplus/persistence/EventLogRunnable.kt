package godau.fynn.usagedirectplus.persistence

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.persistence.HistoryDatabase.Companion.DATABASE_NAME
import godau.fynn.usagedirectplus.wrapper.ComponentForegroundStat
import godau.fynn.usagedirectplus.wrapper.EventLogWrapper
import godau.fynn.usagedirectplus.wrapper.LastUsedConsumer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class EventLogRunnable(private val context: Context) : Runnable {

    override fun run() {
        val sharedPreferences = context.getSharedPreferences(DATABASE_NAME, Context.MODE_PRIVATE)
        val since = sharedPreferences.getLong("lastWrite", 0)

        val database = HistoryDatabase.get(context)
        val usageStats = database.getUsageStatsDao()
        val intervalDao = database.getUsageIntervalDao()

        val eventLogWrapper = EventLogWrapper(context)

        val consumer = LastUsedConsumer()

        // Insert the remainder of the day that contains the timestamp "since" (in current timezone)
        val partialDayStats = eventLogWrapper.getForegroundStatsByPartialDay(since)
        // Aggregate for lastUsed consumer tracking only
        eventLogWrapper.aggregateForegroundStats(partialDayStats, consumer)
        // Insert intervals first (correctly deduplicated via overlap detection)
        intervalDao.insertNonOverlapping(toUsageIntervals(partialDayStats))
        // Recompute stats from all stored intervals for this day (idempotent)
        val zone = ZoneId.systemDefault()
        val sinceDate = Instant.ofEpochMilli(since).atZone(zone).toLocalDate()
        val dayStart = sinceDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = sinceDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        usageStats.replaceFromIntervals(sinceDate.toEpochDay(), intervalDao.getByTimeRange(dayStart, dayEnd))

        // Insert all days following the day that contains "since"
        var nextDay = Instant.ofEpochMilli(since)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .plusDays(1)
            .toEpochDay()

        val today = LocalDate.now().toEpochDay()
        nextDay = maxOf(today - 10, nextDay)

        while (nextDay <= today) {
            val dayStats = eventLogWrapper.getForegroundStatsByDay(nextDay)

            usageStats.insert(
                eventLogWrapper.aggregateForegroundStats(dayStats, consumer)
            )
            intervalDao.insertNonOverlapping(toUsageIntervals(dayStats))

            nextDay++
        }

        // Capture screen events
        captureScreenEvents(database, since)

        database.getLastUsedDao().insert(consumer.applicationLastUsedMap)

        database.close()

        sharedPreferences.edit().putLong("lastWrite", System.currentTimeMillis()).apply()

        schedule()
    }

    /**
     * Queries screen on/off and keyguard events from UsageStatsManager and persists them.
     * Screen events (types 15, 16) require API 25+.
     * Keyguard events (types 17, 18) require API 28+.
     */
    private fun captureScreenEvents(database: HistoryDatabase, since: Long) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return

        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(since, now) ?: return

        val screenEvents = mutableListOf<ScreenEvent>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val type = event.eventType

            // Screen on/off: types 15, 16 (API 25+)
            if (type == ScreenEvent.SCREEN_ON || type == ScreenEvent.SCREEN_OFF) {
                screenEvents.add(ScreenEvent(event.timeStamp, type))
            }

            // Keyguard shown/hidden: types 17, 18 (API 28+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (type == ScreenEvent.KEYGUARD_SHOWN || type == ScreenEvent.KEYGUARD_HIDDEN) {
                    screenEvents.add(ScreenEvent(event.timeStamp, type))
                }
            }
        }

        if (screenEvents.isNotEmpty()) {
            database.getScreenEventDao().insert(screenEvents)
        }
    }

    /**
     * Ensures that the EventLogService job is scheduled
     */
    private fun schedule() {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val scheduled = scheduler.allPendingJobs.size > 0

        if (!scheduled) {
            // Schedule job

            // The permission is granted and the service is registered in the database manifest only
            @SuppressLint("MissingPermission", "JobSchedulerService")
            val jobInfo = JobInfo.Builder(
                EventLogService.JOB_ID, ComponentName(context, EventLogService::class.java)
            )
                .setPeriodic(6 * 60 * 60 * 1000L)
                .setPersisted(true)
                .build()

            val result = scheduler.schedule(jobInfo)

            if (result == JobScheduler.RESULT_FAILURE) {
                Toast.makeText(context, R.string.db_job_schedule_failure, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        /**
         * Converts a list of ComponentForegroundStats into UsageInterval entities.
         */
        private fun toUsageIntervals(stats: List<ComponentForegroundStat>): List<UsageInterval> {
            return stats.map { stat ->
                UsageInterval(stat.beginTime, stat.endTime, stat.packageName)
            }
        }
    }
}
