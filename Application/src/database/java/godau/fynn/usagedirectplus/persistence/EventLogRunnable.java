package godau.fynn.usagedirectplus.persistence;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;
import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.wrapper.ComponentForegroundStat;
import godau.fynn.usagedirectplus.wrapper.EventLogWrapper;
import godau.fynn.usagedirectplus.wrapper.LastUsedConsumer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static godau.fynn.usagedirectplus.persistence.HistoryDatabase.DATABASE_NAME;

public class EventLogRunnable implements Runnable {

    private final Context context;

    public EventLogRunnable(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATABASE_NAME, Context.MODE_PRIVATE);
        long since = sharedPreferences.getLong("lastWrite", 0);

        HistoryDatabase database = HistoryDatabase.get(context);
        UsageStatsDao usageStats = database.getUsageStatsDao();
        UsageIntervalDao intervalDao = database.getUsageIntervalDao();

        EventLogWrapper eventLogWrapper = new EventLogWrapper(context);

        LastUsedConsumer consumer = new LastUsedConsumer();

        // Insert the remainder of the day that contains the timestamp "since" (in current timezone)
        // Also capture raw intervals from the partial day
        List<ComponentForegroundStat> partialDayStats = eventLogWrapper.getForegroundStatsByPartialDay(since);
        usageStats.insertIncremental(
                eventLogWrapper.aggregateForegroundStats(partialDayStats, consumer)
        );
        intervalDao.insertNonOverlapping(toUsageIntervals(partialDayStats));

        // Insert all days following the day that contains "since"
        long nextDay = Instant.ofEpochMilli(since)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(1)
                .toEpochDay();

        long today = LocalDate.now().toEpochDay();
        nextDay = Math.max(today - 10, nextDay);

        while (nextDay <= today) {
            List<ComponentForegroundStat> dayStats = eventLogWrapper.getForegroundStatsByDay(nextDay);

            usageStats.insert(
                    eventLogWrapper.aggregateForegroundStats(dayStats, consumer)
            );
            intervalDao.insertNonOverlapping(toUsageIntervals(dayStats));

            nextDay++;
        }

        // Capture screen events
        captureScreenEvents(database, since);

        database.getLastUsedDao().insert(consumer.applicationLastUsedMap);

        database.close();

        sharedPreferences.edit().putLong("lastWrite", System.currentTimeMillis()).apply();

        schedule();
    }

    /**
     * Converts a list of ComponentForegroundStats into UsageInterval entities.
     */
    private static List<UsageInterval> toUsageIntervals(List<ComponentForegroundStat> stats) {
        List<UsageInterval> intervals = new ArrayList<>(stats.size());
        for (ComponentForegroundStat stat : stats) {
            intervals.add(new UsageInterval(stat.beginTime, stat.endTime, stat.packageName));
        }
        return intervals;
    }

    /**
     * Queries screen on/off and keyguard events from UsageStatsManager and persists them.
     * Screen events (types 15, 16) require API 25+.
     * Keyguard events (types 17, 18) require API 28+.
     */
    private void captureScreenEvents(HistoryDatabase database, long since) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            // Screen events not available before API 25
            return;
        }

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) return;

        long now = System.currentTimeMillis();
        UsageEvents events = usageStatsManager.queryEvents(since, now);
        if (events == null) return;

        List<ScreenEvent> screenEvents = new ArrayList<>();
        UsageEvents.Event event = new UsageEvents.Event();

        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            int type = event.getEventType();

            // Screen on/off: types 15, 16 (API 25+)
            if (type == ScreenEvent.SCREEN_ON || type == ScreenEvent.SCREEN_OFF) {
                screenEvents.add(new ScreenEvent(event.getTimeStamp(), type));
            }

            // Keyguard shown/hidden: types 17, 18 (API 28+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (type == ScreenEvent.KEYGUARD_SHOWN || type == ScreenEvent.KEYGUARD_HIDDEN) {
                    screenEvents.add(new ScreenEvent(event.getTimeStamp(), type));
                }
            }
        }

        if (!screenEvents.isEmpty()) {
            database.getScreenEventDao().insert(screenEvents);
        }
    }

    /**
     * Ensures that the EventLogService job is scheduled
     */
    private void schedule() {
        final JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean scheduled = scheduler.getAllPendingJobs().size() > 0;

        if (!scheduled) {
            // Schedule job

            // The permission is granted and the service is registered in the database manifest only
            @SuppressLint({"MissingPermission", "JobSchedulerService"})
            JobInfo jobInfo = new JobInfo.Builder(
                    EventLogService.JOB_ID, new ComponentName(context, EventLogService.class)
            )
                    .setPeriodic(6 * 60 * 60 * 1000)
                    .setPersisted(true)
                    .build();

            int result = scheduler.schedule(jobInfo);

            if (result == JobScheduler.RESULT_FAILURE) {
                Toast.makeText(context, R.string.db_job_schedule_failure, Toast.LENGTH_LONG).show();
            }
        }
    }
}
