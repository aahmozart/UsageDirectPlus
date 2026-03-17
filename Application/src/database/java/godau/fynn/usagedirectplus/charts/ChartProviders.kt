package godau.fynn.usagedirectplus.charts

import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.view.adapter.ChartSelectionAdapter

object ChartProviders {

    @JvmStatic
    fun getChartProviders(): List<ChartSelectionAdapter.ChartProvider> {
        return listOf(
            ChartSelectionAdapter.ChartProvider(R.string.charts_bar_daily, DailyBarChart::class.java),
            ChartSelectionAdapter.ChartProvider(R.string.charts_bar_daily_condensed, DailyCondensedBarChart::class.java),
            ChartSelectionAdapter.ChartProvider(R.string.chart_average, WeeklyAverageBarChart::class.java),
            ChartSelectionAdapter.ChartProvider(R.string.charts_clock_pie_general, ClockPieCharts::class.java),
            ChartSelectionAdapter.ChartProvider(R.string.charts_usage_timeline_general, UsageTimelineChart::class.java)
        )
    }
}
