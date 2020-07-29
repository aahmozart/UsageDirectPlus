/*
 * usageDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package godau.fynn.usagedirect.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.viewpager.widget.PagerAdapter;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;
import im.dacer.androidcharts.BarView;

import java.util.*;

public class BarViewPagerAdapter extends PagerAdapter {

    private final Context context;
    private final UsageStatsWrapper usageStatsWrapper;

    private final FramedBarView[] viewArray = new FramedBarView[4];

    private int boldPosition = 9;

    public BarViewPagerAdapter(Context context, UsageStatsWrapper usageStatsWrapper) {
        this.context = context;
        this.usageStatsWrapper = usageStatsWrapper;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        FramedBarView barViewFrame = new FramedBarView(context);
        container.addView(barViewFrame);

        viewArray[position] = barViewFrame;

        Interval interval = Interval.values()[position];
        int datasetAmount = usageStatsWrapper.getDatasetAmount(interval) - 1;

        @StringRes int text = 0;
        switch (interval) {
            case DAILY:
                text = R.string.bar_chart_daily;
                break;
            case WEEKLY:
                text = R.string.bar_chart_weekly;
                break;
            case MONTHLY:
                text = R.string.bar_chart_monthly;
                break;
            case YEARLY:
                text = R.string.bar_chart_yearly;
                break;
        }

        barViewFrame.setText(context.getString(text));

        List<Integer> accumulatedTimes = usageStatsWrapper.getAccumulatedTimes(
                interval, datasetAmount);

        if (accumulatedTimes.size() == 0) {
            Toast.makeText(context, R.string.error_no_data, Toast.LENGTH_LONG).show();
            return barViewFrame;
        }

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int max = Collections.max(accumulatedTimes);
        int chartMax = max + (60 * 30);

        ArrayList<String> bottomText = new ArrayList<>();

        for (int i = datasetAmount; i >= 0; i--) {
            bottomText.add(NaturalText.formatShort(interval, i));
        }

        BarView barView = barViewFrame.getBarView();

        barView.setDataList(accumulatedTimes, chartMax);
        barView.setBottomTextList(bottomText);

        // Set bottom text as bold according to selected item of clock pie chart
        if (interval == Interval.DAILY) {
            barView.setBoldPosition(boldPosition);
        }

        // Calculate vertical line frequency
        int maxHours = (max / 60 / 60) + 1;
        int frequency = 1;
        while (maxHours / 15 > frequency) {
            frequency *= 10;
        }

        // Add lines
        List<Integer> lines = new ArrayList<>();
        int counter = frequency;
        do {
            lines.add(counter * 60 * 60);
        } while ((counter += frequency) < maxHours);

        barView.setVerticalLines(lines, chartMax);

        barViewFrame.scrollToEnd();

        return barViewFrame;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        viewArray[position] = null;
        //recycleViewList.add((FramedClockPieView) object);
    }

    @Override
    public int getCount() {
        return Interval.values().length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public void setDailyBoldPosition(int position) {
        boldPosition = position;
        if (viewArray[0] != null) {
            viewArray[0].getBarView().setBoldPosition(position);
        }
    }
}
