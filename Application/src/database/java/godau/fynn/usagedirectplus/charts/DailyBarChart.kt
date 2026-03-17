package godau.fynn.usagedirectplus.charts

import androidx.annotation.StringRes
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import im.dacer.androidcharts.bar.MultiValue
import im.dacer.androidcharts.bar.Value
import java.time.LocalDate

open class DailyBarChart : UsageStatBarChart() {

    private lateinit var values: Array<Value>
    private var chartMax: Int = 0

    @StringRes
    override fun getText(): Int {
        return R.string.charts_bar_daily
    }

    /**
     * Queries data that is then used for the chart. Furthermore calculates
     * data for the bar view for each of the days in the `days`
     * array, in its order.
     *
     * The label is gathered from [getLabel].
     */
    override fun getData(database: HistoryDatabase) {
        val usageStatsDao = database.getUsageStatsDao()

        val minDay = usageStatsDao.getMinimumDay()
        val maxDay = usageStatsDao.getMaximumDay()

        val displayDays = LongArray(((maxDay - minDay) + 1).toInt())
        var i = 0
        var day = minDay
        while (day <= maxDay) {
            displayDays[i] = day
            i++
            day++
        }

        val coloredUsageStats = database.getAppColorDao().getColoredUsageStats()

        // Collect data and labels

        values = Array(displayDays.size) { Value(0, null) }

        i = 0
        var max = 0
        for (d in displayDays) {
            val seconds = ArrayList<Int>()
            val colors = ArrayList<Int?>()

            var uncoloredSeconds = 0

            // Gather usage stats for this day
            for (coloredSimpleUsageStat in coloredUsageStats) {
                if (coloredSimpleUsageStat.day != d) continue

                if (coloredSimpleUsageStat.color == null) {
                    uncoloredSeconds += (coloredSimpleUsageStat.timeUsed / 1000).toInt()
                    continue
                }

                seconds.add((coloredSimpleUsageStat.timeUsed / 1000).toInt())
                colors.add(coloredSimpleUsageStat.color)
            }

            seconds.add(uncoloredSeconds)
            colors.add(null)

            val date = LocalDate.ofEpochDay(d)

            values[i++] = MultiValue(
                seconds.stream().mapToInt { it }.toArray(),
                colors.toTypedArray(),
                getLabel(date)
            )

            val dayTotal = seconds.stream().mapToInt { it }.sum()
            if (dayTotal > max) max = dayTotal
        }

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        chartMax = max + (60 * 30)
    }

    /**
     * Displays the data that is calculated in [getData]
     * in the chart view.
     */
    override fun onDataLoaded() {
        barView.setData(values, chartMax)

        barView.scrollToEnd()

        // Kinda hacky - we want to avoid an additional method call
        // Don't add scale for subclasses
        if (this.javaClass == DailyBarChart::class.java) {
            addScale(chartMax)
        }
    }

    /**
     * This method call should be overwritten by subclasses and determines the label
     * that a specific `date` should be shown with in the chart.
     *
     * @param date Date for which a label must be generated
     * @return `null` in case of no label
     */
    protected open fun getLabel(date: LocalDate): String? {
        return date.dayOfMonth.toString()
    }
}
