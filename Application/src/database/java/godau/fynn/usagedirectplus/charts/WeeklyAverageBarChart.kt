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

    @Dao
    abstract class WeeklyAverageDao {

        @Query(
            "SELECT usageStats.day AS day, usageStats.timeUsed AS timeUsed, apps.applicationId AS applicationId, usageStats.hidden AS hidden " +
                "FROM usageStats " +
                "INNER JOIN apps ON apps.id = usageStats.appId " +
                "WHERE usageStats.hidden = 0"
        )
        protected abstract fun getUsageStats(): Array<SimpleUsageStat>

        @Query(
            "SELECT DISTINCT apps.applicationId AS applicationId, colors.color AS color, colors.priority AS priority " +
                "FROM colors " +
                "INNER JOIN apps ON apps.id = colors.appId " +
                // Only return colors that actually appear in the usageStats to avoid NullPointerExceptions
                // in connection with the Map that is filled using `getAllApplicationIds()`
                "INNER JOIN usageStats ON usageStats.appId == colors.appId " +
                "ORDER BY colors.priority DESC"
        )
        protected abstract fun getColors(): Array<AppColor>

        @Query(
            "SELECT DISTINCT apps.applicationId " +
                "FROM usageStats " +
                "INNER JOIN apps ON apps.id = usageStats.appId"
        )
        protected abstract fun getAllApplicationIds(): Array<String>

        @Transaction
        open fun getData(): Array<Value> {
            val values = arrayOfNulls<Value>(DayOfWeek.values().size)

            val usageStats = getUsageStats()
            val allApplicationIds = getAllApplicationIds()
            val usageByDay = LinkedHashMap<Long, MutableList<SimpleUsageStat>>()
            for (stat in usageStats) {
                usageByDay.getOrPut(stat.day) { mutableListOf() }.add(stat)
            }

            for (day in DayOfWeek.values().indices) {
                val weekday = DayOfWeek.values()[day]

                // Prepare sum map by setting each existing package name to zero
                val applicationSum = HashMap<String, Long>()
                for (applicationId in allApplicationIds) {
                    applicationSum[applicationId] = 0L
                }

                // Count days for average calculation
                var daysConsidered = 0
                for ((epochDay, statsForDay) in usageByDay) {
                    // Skip days that are not of the correct weekday
                    if (LocalDate.ofEpochDay(epochDay).dayOfWeek != weekday) continue
                    else daysConsidered++

                    for (stat in statsForDay) {
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
