package godau.fynn.usagedirect.charts;

import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.activity.fragment.SystemChartsFragment;
import godau.fynn.usagedirect.view.adapter.ChartSelectionAdapter;

import java.util.Arrays;
import java.util.List;

public class ChartProviders {

    public static List<ChartSelectionAdapter.ChartProvider> getChartProviders() {
        return Arrays.asList(
                new ChartSelectionAdapter.ChartProvider(R.string.bar_chart_daily, DailyBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.charts_clock_pie_general, ClockPieCharts.class),
                new ChartSelectionAdapter.ChartProvider(R.string.chart_average, WeeklyAverageBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.more_charts, SystemChartsFragment.class)
        );
    }
}