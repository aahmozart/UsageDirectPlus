package godau.fynn.usagedirectplus.charts

import androidx.annotation.ColorInt
import androidx.room.*
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.persistence.AppColor
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.wrapper.TextFormat
import im.dacer.androidcharts.bar.MultiValue
import im.dacer.androidcharts.bar.Value
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Arrays

class WeeklyAverageBarChart : UsageStatBarChart() {

    private lateinit var usagePerDayMap: Array<Value>

    override fun getText(): Int {
        return R.string.chart_average
    }

    override fun getData(database: HistoryDatabase) {
        usagePerDayMap = database.getWeeklyDao().getData()
    }

    override fun onDataLoaded() {
        val max = Arrays.stream(usagePerDayMap)
            .mapToInt { v -> v.value }
            .max().asInt

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        val chartMax = max + (60 * 30)

        barView.setData(usagePerDayMap, chartMax)

        addScale(chartMax)
    }

    class UsageDay {
        var day: Long = 0
        @Relation(
            parentColumn = "day",
            entityColumn = "day",
            entity = SimpleUsageStat::class
        )
        lateinit var usageStats: List<SimpleUsageStat>
    }

    @Dao
    abstract class WeeklyAverageDao {

        @Query(
            "SELECT day, timeUsed, usageStats.applicationId AS applicationId FROM usageStats " +
                    "LEFT JOIN colors ON usageStats.applicationId == colors.applicationId " +
                    "WHERE hidden = 0 " +
                    "GROUP BY day"
        )
        protected abstract fun getUsageDays(): Array<UsageDay>

        @Query(
            "SELECT DISTINCT colors.applicationId, color, priority FROM colors " +
                    // Only return colors that actually appear in the usageStats to avoid NullPointerExceptions
                    // in connection with the Map that is filled using `getAllApplicationIds()`
                    "INNER JOIN usageStats ON usageStats.applicationId == colors.applicationId " +
                    "ORDER BY priority DESC"
        )
        protected abstract fun getColors(): Array<AppColor>

        @Query("SELECT DISTINCT applicationId FROM usageStats")
        protected abstract fun getAllApplicationIds(): Array<String>

        @Transaction
        open fun getData(): Array<Value> {
            val values = arrayOfNulls<Value>(DayOfWeek.values().size)

            // Query usage data for all days (grouped by day in UsageDay object)
            val usageDays = getUsageDays()
            val allApplicationIds = getAllApplicationIds()

            for (day in DayOfWeek.values().indices) {
                val weekday = DayOfWeek.values()[day]

                // Prepare sum map by setting each existing package name to zero
                val applicationSum = HashMap<String, Long>()
                for (applicationId in allApplicationIds) {
                    applicationSum[applicationId] = 0L
                }

                // Count days for average calculation
                var daysConsidered = 0
                for (usageDay in usageDays) {
                    // Skip days that are not of the correct weekday
                    if (LocalDate.ofEpochDay(usageDay.day).dayOfWeek != weekday) continue
                    else daysConsidered++

                    for (stat in usageDay.usageStats) {
                        applicationSum[stat.applicationId] =
                            applicationSum[stat.applicationId]!! + stat.timeUsed
                    }
                }

                // Pull values for colored apps
                val colors = getColors()

                @ColorInt val colorInts = arrayOfNulls<Int>(colors.size + 1)
                val averageTimes = IntArray(colors.size + 1)

                for (i in colors.indices) {
                    val color = colors[i]

                    colorInts[i] = color.color

                    if (daysConsidered > 0) {
                        averageTimes[i] = (applicationSum[color.applicationId]!! / 1000 / daysConsidered).toInt()
                    } else {
                        averageTimes[i] = 0
                    }

                    // Remove from map
                    applicationSum.remove(color.applicationId)
                }

                // Add values for uncolored apps (all remaining values in map)
                colorInts[colors.size] = null
                val finalDaysConsidered = daysConsidered
                if (finalDaysConsidered > 0) {
                    averageTimes[colors.size] = applicationSum.values
                        .stream()
                        .mapToInt { l -> (l / 1000 / finalDaysConsidered).toInt() }
                        .sum()
                } else {
                    averageTimes[colors.size] = 0
                }

                // Construct MultiValue for weekday
                values[day] = MultiValue(
                    averageTimes, colorInts, TextFormat.formatWeekday(weekday)
                )
            }

            @Suppress("UNCHECKED_CAST")
            return values as Array<Value>
        }
    }
}
