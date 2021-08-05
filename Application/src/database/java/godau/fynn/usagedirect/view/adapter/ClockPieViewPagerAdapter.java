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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClockPieViewPagerAdapter extends PagerAdapter {

    private final Context context;
    private final EventLogWrapper eventLogWrapper;
    private final Map<String, Integer> colorMap;
    private static final Queue<FramedClockPieView> recycleViewList = new LinkedBlockingQueue<>();

    /**
     * For performance, don't instantiate the first two pages that are not actually displayed.
     *
     * @see <a href="https://codeberg.org/fynngodau/usageDirect/issues/55">related issue</a>
     */
    private boolean fakeInstantiateFirstPages = true;


    public ClockPieViewPagerAdapter(Context context, EventLogWrapper wrapper, Map<String, Integer> colorMap) {
        this.context = context;
        eventLogWrapper = wrapper;
        this.colorMap = colorMap;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        // "This does not need to be a View[…]"
        if (fakeInstantiateFirstPages && position < 2) {
            Log.d("CPVPA", "Faking item at position " + position);
            return new Object();
        }

        if (position > 5) fakeInstantiateFirstPages = false;

        final FramedClockPieView clockPieFrame;
        if (recycleViewList.peek() == null) {
            clockPieFrame = new FramedClockPieView(context);
            Log.d("CPVPA", "Creating clock pie frame view for position " + position);
        } else {
            Log.d("CPVPA", "Recycling clock pie frame view from recycle bin for position " + position);
            clockPieFrame = recycleViewList.poll();
            clockPieFrame.getClockPieView().setData(new ClockPieSegment[0]);
        }
        container.addView(clockPieFrame);

        clockPieFrame.setText(context.getString(R.string.charts_clock_pie,
                TextFormat.formatDay(getCount() - 1 - position, context.getResources())
        ));

        final ClockPieView pieView = clockPieFrame.getClockPieView();

        ClockPieSegment backgroundSegment;
        if (position == getCount() - 1) {
            LocalTime now = LocalTime.now();
            backgroundSegment = new ClockPieSegment(0, 0, 0, now.getHour(), now.getMinute(), now.getSecond());
        } else {
            backgroundSegment = new ClockPieSegment(0, 0, 24, 0);
        }
        pieView.setBackgroundSegment(backgroundSegment);

        final ArrayList<ClockPieSegment> clockPieHelperList = new ArrayList<>();

        new Thread(() -> {

            final List<ComponentForegroundStat> foregroundStats = eventLogWrapper.getForegroundStatsByRelativeDay(getCount() - 1 - position);

            new Handler(Looper.getMainLooper()).post(() -> {
                for (ComponentForegroundStat stat : foregroundStats) {
                    ClockPieSegment segment = stat.asClockPieSegment();

                    if (colorMap.containsKey(stat.packageName)) {
                        segment.setColor(colorMap.get(stat.packageName));
                    }

                    clockPieHelperList.add(segment);
                }

                pieView.setData(clockPieHelperList);
            });
        }).start();


        return clockPieFrame;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object instanceof View) {
            container.removeView((View) object);
            recycleViewList.add((FramedClockPieView) object);
        } // Discard non-View placeholder Objects
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
