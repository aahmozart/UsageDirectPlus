package godau.fynn.usagedirect.charts;

import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.view.adapter.ChartSelectionAdapter;

import java.util.Arrays;
import java.util.List;

public class ChartProviders {

    public static List<ChartSelectionAdapter.ChartProvider> getChartProviders() {
        return Arrays.asList(
                new ChartSelectionAdapter.ChartProvider(R.string.charts_bar_daily, PeriodicBarChart.DailyBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.charts_bar_weekly, PeriodicBarChart.WeeklyBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.charts_bar_monthly, PeriodicBarChart.MonthlyBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.charts_bar_yearly, PeriodicBarChart.YearlyBarChart.class),
                new ChartSelectionAdapter.ChartProvider(R.string.charts_clock_pie_general, ClockPieCharts.class)
        );
    }
}
