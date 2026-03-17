package godau.fynn.usagedirectplus

import android.app.usage.UsageStats

object Comparator {
    class LastTimeLaunchedComparatorDesc : java.util.Comparator<UsageStats> {
        override fun compare(left: UsageStats, right: UsageStats): Int {
            return java.lang.Long.compare(right.lastTimeUsed, left.lastTimeUsed)
        }
    }

    class TimeInForegroundComparatorDesc : java.util.Comparator<SimpleUsageStat> {
        override fun compare(left: SimpleUsageStat, right: SimpleUsageStat): Int {
            return java.lang.Long.compare(right.timeUsed, left.timeUsed)
        }
    }
}
