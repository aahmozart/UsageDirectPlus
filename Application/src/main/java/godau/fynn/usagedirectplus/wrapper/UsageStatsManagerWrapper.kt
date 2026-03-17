package godau.fynn.usagedirectplus.wrapper

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import godau.fynn.usagedirectplus.SimpleUsageStat

open class UsageStatsManagerWrapper @SuppressLint("WrongConstant") constructor(
    protected val context: Context
) {
    init {
        usageStatsManager = context.getSystemService("usagestats") as UsageStatsManager
    }

    /**
     * Tests whether usage stats permission has been granted by the user.
     * If not, user needs to be prompted to grant permission in settings.
     *
     * @see [StackOverflow](https://stackoverflow.com/a/28921586)
     */
    fun isPermissionGranted(): Boolean {
        return isPermissionGranted(context)
    }

    companion object {
        @JvmStatic
        protected var usageStatsManager: UsageStatsManager? = null

        @JvmStatic
        fun aggregateSimpleUsageStats(usageStats: List<SimpleUsageStat>): Long {
            var sum = 0L
            for (usageStat in usageStats) {
                sum += usageStat.timeUsed
            }
            return sum
        }

        /**
         * Tests whether usage stats permission has been granted by the user.
         * If not, user needs to be prompted to grant permission in settings.
         */
        @JvmStatic
        fun isPermissionGranted(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }
    }
}
