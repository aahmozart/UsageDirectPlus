package godau.fynn.usagedirect.charts;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.activity.fragment.UsageStatBarFragment;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;
import im.dacer.androidcharts.bar.Value;

import java.util.Collections;
import java.util.List;

public abstract class PeriodicBarChart extends UsageStatBarFragment {

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(getContext());

        Interval interval = getInterval();
        int datasetAmount = usageStatsWrapper.getDatasetAmount(interval) - 1;

        List<Integer> accumulatedTimes = usageStatsWrapper.getAccumulatedTimes(
                interval, datasetAmount);

        if (accumulatedTimes.size() == 0) {
            Toast.makeText(getContext(), R.string.error_no_data, Toast.LENGTH_LONG).show();
        }

        setText(getTitle());
        setSystemData(accumulatedTimes, interval);

        scrollToEnd();
    }

    /**
     * Set the bar view's data to the provided list of accumulated times.
     * The last integer is assumed to be for the currently ongoing period,
     * the previous integers to be the respective periods before that.
     * Also adds scale to bar view.
     *
     * @param interval Interval for bottom text calculation
     */
    private void setSystemData(List<Integer> accumulatedTimes, Interval interval) {


        Value[] values = new Value[accumulatedTimes.size()];

        for (int i = 0; i < accumulatedTimes.size(); i++) {
            values[i] = new Value(accumulatedTimes.get(i),
                    NaturalText.formatShort(interval, accumulatedTimes.size() - 1 - i)
            );
        }

        int max = Collections.max(accumulatedTimes);

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        barView.setData(values, chartMax);

        addScale(max, chartMax);
    }

    protected abstract @StringRes int getTitle();

    protected abstract Interval getInterval();

    public static class DailyBarChart extends PeriodicBarChart {
        @Override
        protected int getTitle() {
            return R.string.bar_chart_daily;
        }

        @Override
        protected Interval getInterval() {
            return Interval.DAILY;
        }
    }

    public static class WeeklyBarChart extends PeriodicBarChart {
        @Override
        protected int getTitle() {
            return R.string.bar_chart_weekly;
        }

        @Override
        protected Interval getInterval() {
            return Interval.WEEKLY;
        }
    }

    public static class MonthlyBarChart extends PeriodicBarChart {
        @Override
        protected int getTitle() {
            return R.string.bar_chart_monthly;
        }

        @Override
        protected Interval getInterval() {
            return Interval.MONTHLY;
        }
    }

    public static class YearlyBarChart extends PeriodicBarChart {
        @Override
        protected int getTitle() {
            return R.string.bar_chart_yearly;
        }

        @Override
        protected Interval getInterval() {
            return Interval.YEARLY;
        }
    }
}
