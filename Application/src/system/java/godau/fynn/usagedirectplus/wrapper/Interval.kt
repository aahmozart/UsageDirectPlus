package godau.fynn.usagedirectplus.wrapper

import android.app.usage.UsageStatsManager
import java.util.Calendar

enum class Interval(
    @JvmField val interval: Int,
    private val calendarField: Int
) {
    DAILY(UsageStatsManager.INTERVAL_DAILY, Calendar.DAY_OF_MONTH),
    WEEKLY(UsageStatsManager.INTERVAL_WEEKLY, Calendar.WEEK_OF_MONTH),
    MONTHLY(UsageStatsManager.INTERVAL_MONTHLY, Calendar.MONTH),
    YEARLY(UsageStatsManager.INTERVAL_YEARLY, Calendar.YEAR);

    fun backInTime(times: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.add(calendarField, -times)
        return calendar
    }
}
