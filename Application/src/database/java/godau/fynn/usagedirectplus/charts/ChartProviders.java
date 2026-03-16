package godau.fynn.usagedirectplus.charts;

import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.view.adapter.ChartSelectionAdapter;

import java.util.Arrays;
import java.util.List;

public class ChartProviders {

    public static List<ChartSelectionAdapter.ChartProvider> getChartProviders() {
        return Arrays.asList(
                new ChartSelectionAdapter.ChartProvider(R.string.charts_bar_daily, DailyBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.charts_bar_daily_condensed, DailyCondensedBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.chart_average, WeeklyAverageBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.charts_clock_pie_general, ClockPieCharts.class)
        );
    }
}