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

package godau.fynn.usagedirect.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.view.FramedClockPieView;
import godau.fynn.usagedirect.wrapper.ComponentForegroundStat;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;
import im.dacer.androidcharts.clockpie.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ClockPieViewPagerAdapter extends PagerAdapter {

    private final Context context;
    private final UsageStatsWrapper usageStatsWrapper;
    private static final Queue<FramedClockPieView> recycleViewList = new LinkedBlockingQueue<>();


    public ClockPieViewPagerAdapter(Context context, UsageStatsWrapper wrapper) {
        this.context = context;
        usageStatsWrapper = wrapper;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        final FramedClockPieView clockPieFrame;
        if (recycleViewList.peek() == null) {
            clockPieFrame = new FramedClockPieView(context);
        } else {
            Log.d("CPVPA", "Recycling clock pie frame view from recycle bin");
            clockPieFrame = recycleViewList.poll();
        }
        container.addView(clockPieFrame);

        clockPieFrame.setText(context.getString(R.string.charts_clock_pie,
                NaturalText.format(Interval.DAILY, getCount() - 1 - position, context)
        ));

        ClockPieView pieView = clockPieFrame.getClockPieView();

        ArrayList<ClockPieSegment> clockPieHelperList = new ArrayList<>();

        List<ComponentForegroundStat> foregroundStats = usageStatsWrapper.getForegroundStatsByRelativeDay(getCount() - 1 - position);

        Calendar beginCalendar = Calendar.getInstance();
        beginCalendar.setTimeZone(usageStatsWrapper.getTimezone());
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeZone(usageStatsWrapper.getTimezone());
        for (ComponentForegroundStat stat : foregroundStats) {
            beginCalendar.setTimeInMillis(stat.beginTime);
            endCalendar.setTimeInMillis(stat.endTime);
            clockPieHelperList.add(new ClockPieSegment(
                    beginCalendar.get(Calendar.HOUR_OF_DAY), beginCalendar.get(Calendar.MINUTE), beginCalendar.get(Calendar.SECOND),
                    endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), endCalendar.get(Calendar.SECOND)
            ));
        }

        Log.d("ChartsActivity", "Displaying " + foregroundStats.size() + " foreground stats");

        pieView.setData(clockPieHelperList);

        return clockPieFrame;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        recycleViewList.add((FramedClockPieView) object);
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
