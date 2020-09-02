package godau.fynn.usagedirect.persistence;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.wrapper.EventLogWrapper;

import java.time.Instant;
import java.time.ZoneId;

import static godau.fynn.usagedirect.persistence.HistoryDatabase.DATABASE_NAME;

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

        EventLogWrapper eventLogWrapper = new EventLogWrapper(context);

        // Insert the remainder of the day that contains the timestamp "since" (in current timezone)
        usageStats.insertIncremental(
                eventLogWrapper.getIncrementalSimpleUsageStats(since)
        );

        // Insert all days following the day that contains "since"
        usageStats.insert(
                eventLogWrapper.getAllSimpleUsageStats(
                        Instant.ofEpochMilli(since)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .plusDays(1)
                                .toEpochDay()
                )
        );

        database.close();

        sharedPreferences.edit().putLong("lastWrite", System.currentTimeMillis()).apply();

        schedule();
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
                    .setPeriodic(24 * 60 * 60 * 1000)
                    .setPersisted(true)
                    .build();

            int result = scheduler.schedule(jobInfo);

            if (result == JobScheduler.RESULT_FAILURE) {
                Toast.makeText(context, R.string.db_job_schedule_failure, Toast.LENGTH_LONG).show();
            }
        }
    }
}
