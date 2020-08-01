package godau.fynn.usagedirect.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;

import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

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

        HistoryDatabase database = Room.databaseBuilder(context, HistoryDatabase.class, DATABASE_NAME).build();
        UsageStatsDao usageStats = database.getUsageStatsDao();

        UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(context);
        usageStats.insert(
                usageStatsWrapper.getAllSimpleUsageStats(new Day(since, usageStatsWrapper.getTimezone()))
        );

        sharedPreferences.edit().putLong("lastWrite", System.currentTimeMillis()).apply();
    }
}
