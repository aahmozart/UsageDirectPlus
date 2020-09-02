package godau.fynn.usagedirect.wrapper;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.util.List;
import java.util.TimeZone;

public abstract class UsageStatsManagerWrapper {

    protected final Context context;
    protected static UsageStatsManager usageStatsManager;

    @SuppressLint("WrongConstant")
    public UsageStatsManagerWrapper(Context context) {
        this.context = context;
        usageStatsManager = (UsageStatsManager) context
                .getSystemService("usagestats"); // Context.USAGE_STATS_SERVICE
    }

    public static long aggregateSimpleUsageStats(List<SimpleUsageStat> usageStats) {
        long sum = 0;

        for (SimpleUsageStat usageStat : usageStats) {
            sum += usageStat.getTimeUsed();
        }

        return sum;
    }

    /**
     * Tests whether usage stats permission has been granted by the user.
     * If not, user needs to be prompted to grant permission in settings.
     *
     * @see <a href="https://stackoverflow.com/a/28921586">StackOverflow</a>
     */
    public boolean isPermissionGranted() {
        return isPermissionGranted(context);
    }

    /**
     * Tests whether usage stats permission has been granted by the user.
     * If not, user needs to be prompted to grant permission in settings.
     */
    public static boolean isPermissionGranted(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
