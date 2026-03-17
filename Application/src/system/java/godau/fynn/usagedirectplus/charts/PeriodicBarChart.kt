package godau.fynn.usagedirectplus.charts

import android.widget.Toast
import androidx.annotation.StringRes
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.wrapper.Interval
import godau.fynn.usagedirectplus.wrapper.IntervalTextFormat
import godau.fynn.usagedirectplus.wrapper.UsageStatsWrapper
import im.dacer.androidcharts.bar.Value
import java.util.Collections

abstract class PeriodicBarChart : UsageStatBarChart() {

    private lateinit var accumulatedTimes: List<Int>

    override fun getData(database: HistoryDatabase) {
        val usageStatsWrapper = UsageStatsWrapper(context!!)

        val interval = getInterval()
        val datasetAmount = usageStatsWrapper.getDatasetAmount(interval) - 1

        accumulatedTimes = usageStatsWrapper
            .getAccumulatedTimes(interval, datasetAmount)
    }

    override fun onDataLoaded() {
        if (accumulatedTimes.isEmpty()) {
            Toast.makeText(context, R.string.error_no_data, Toast.LENGTH_LONG).show()
        }

        setSystemData(accumulatedTimes, getInterval())

        barView.scrollToEnd()
    }

    /**
     * Set the bar view's data to the provided list of accumulated times.
     * The last integer is assumed to be for the currently ongoing period,
     * the previous integers to be the respective periods before that.
     * Also adds scale to bar view.
     *
     * @param interval Interval for bottom text calculation
     */
    private fun setSystemData(accumulatedTimes: List<Int>, interval: Interval) {
        val values = Array(accumulatedTimes.size) { i ->
            Value(
                accumulatedTimes[i],
                IntervalTextFormat.formatShort(interval, accumulatedTimes.size - 1 - i)
            )
        }

        val max = Collections.max(accumulatedTimes)

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        val chartMax = max + (60 * 30)

        barView.setData(values, chartMax)

        addScale(chartMax)
    }

    protected abstract fun getInterval(): Interval

    class DailyBarChart : PeriodicBarChart() {
        @StringRes
        override fun getText(): Int {
            return R.string.charts_bar_daily
        }

        override fun getInterval(): Interval {
            return Interval.DAILY
        }
    }

    class WeeklyBarChart : PeriodicBarChart() {
        @StringRes
        override fun getText(): Int {
            return R.string.charts_bar_weekly
        }

        override fun getInterval(): Interval {
            return Interval.WEEKLY
        }
    }

    class MonthlyBarChart : PeriodicBarChart() {
        @StringRes
        override fun getText(): Int {
            return R.string.charts_bar_monthly
        }

        override fun getInterval(): Interval {
            return Interval.MONTHLY
        }
    }

    class YearlyBarChart : PeriodicBarChart() {
        @StringRes
        override fun getText(): Int {
            return R.string.charts_bar_yearly
        }

        override fun getInterval(): Interval {
            return Interval.YEARLY
        }
    }
}
