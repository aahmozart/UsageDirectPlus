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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.view.FramedClockPieView;
import godau.fynn.usagedirect.wrapper.ComponentForegroundStat;
import godau.fynn.usagedirect.wrapper.EventLogWrapper;
import godau.fynn.usagedirect.wrapper.TextFormat;
import im.dacer.androidcharts.clockpie.ClockPieSegment;
import im.dacer.androidcharts.clockpie.ClockPieView;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClockPieViewPagerAdapter extends PagerAdapter {

    private final Context context;
    private final EventLogWrapper eventLogWrapper;
    private static final Queue<FramedClockPieView> recycleViewList = new LinkedBlockingQueue<>();


    public ClockPieViewPagerAdapter(Context context, EventLogWrapper wrapper) {
        this.context = context;
        eventLogWrapper = wrapper;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        final FramedClockPieView clockPieFrame;
        if (recycleViewList.peek() == null) {
            clockPieFrame = new FramedClockPieView(context);
        } else {
            Log.d("CPVPA", "Recycling clock pie frame view from recycle bin");
            clockPieFrame = recycleViewList.poll();
            clockPieFrame.getClockPieView().setData(new ClockPieSegment[0]);
        }
        container.addView(clockPieFrame);

        clockPieFrame.setText(context.getString(R.string.charts_clock_pie,
                TextFormat.formatDay(getCount() - 1 - position, context.getResources())
        ));

        final ClockPieView pieView = clockPieFrame.getClockPieView();

        final ArrayList<ClockPieSegment> clockPieHelperList = new ArrayList<>();

        new Thread(() -> {

            final List<ComponentForegroundStat> foregroundStats = eventLogWrapper.getForegroundStatsByRelativeDay(getCount() - 1 - position);

            new Handler(Looper.getMainLooper()).post(() -> {
                for (ComponentForegroundStat stat : foregroundStats) {
                    clockPieHelperList.add(stat.asClockPieSegment());
                }

                pieView.setData(clockPieHelperList);
            });
        }).start();


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
