package godau.fynn.usagedirectplus.charts

import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.view.adapter.ChartSelectionAdapter

object ChartProviders {

    @JvmStatic
    fun getChartProviders(): List<ChartSelectionAdapter.ChartProvider> {
        return listOf(
            ChartSelectionAdapter.ChartProvider(R.string.charts_bar_daily, PeriodicBarChart.DailyBarChart::class.java),
            ChartSelectionAdapter.ChartProvider(R.string.charts_bar_weekly, PeriodicBarChart.WeeklyBarChart::class.java),
            ChartSelectionAdapter.ChartProvider(R.string.charts_bar_monthly, PeriodicBarChart.MonthlyBarChart::class.java),
            ChartSelectionAdapter.ChartProvider(R.string.charts_bar_yearly, PeriodicBarChart.YearlyBarChart::class.java)
        )
    }
}
