package godau.fynn.usagedirect.charts;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import godau.fynn.usagedirect.R;
import im.dacer.androidcharts.bar.CondensedBarView;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public class DailyCondensedBarChart extends DailyBarChart {

    @Override
    protected int getLayout() {
        return R.layout.content_bar_view_condensed;
    }

    @Override
    protected @StringRes
    int getText() {
        return R.string.charts_bar_daily_condensed;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barView.setZeroLineEnabled(true);
        ((CondensedBarView) barView).setBarWidth(8);
        ((CondensedBarView) barView).setLabelIndicatorMode(CondensedBarView.LabelIndicatorMode.IN_CHART);

    }

    @Override
    protected String getLabel(LocalDate date) {
        WeekFields week = WeekFields.ISO;

        // Add label at the beginning of each week
        if (date.getDayOfWeek() == week.getFirstDayOfWeek()) {
            return String.valueOf(date.get(week.weekOfYear()));
        } else return null;
    }
}
