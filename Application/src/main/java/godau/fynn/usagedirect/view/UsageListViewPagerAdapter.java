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

import android.app.Activity;
import android.app.usage.UsageStats;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import godau.fynn.usagedirect.Comparator;
import godau.fynn.usagedirect.IconThread;
import godau.fynn.usagedirect.persistence.SimpleUsageStat;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Child pager of {@link TimespanPagerAdapter}, pages {@link UsageListView}s
 */
public abstract class UsageListViewPagerAdapter extends PagerAdapter {
    private static final Queue<UsageListView> recycleViewList = new LinkedBlockingQueue<>();
    protected final Activity context;

    public UsageListViewPagerAdapter(Activity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        // Setup view

        final UsageListView usageListView;
        if (recycleViewList.peek() == null) {
            usageListView = new UsageListView(context);
        } else {
            Log.d("ULVPA", "Recycling usage list view from recycle bin");
            usageListView = recycleViewList.poll();
            usageListView.setUsageStatsList(null);
        }
        container.addView(usageListView);

        usageListView.setShowLastUsed(position == getCount() - 1);


        // Get data

        new Thread(new Runnable() {
            @Override
            public void run() {

                final List<SimpleUsageStat> usageStatsList = getUsageStats(position);

                // Filter unused apps
                for (int i = usageStatsList.size() - 1; i >= 0; i--) {
                    SimpleUsageStat usageStat = usageStatsList.get(i);
                    if (usageStat.getTimeUsed() <= 0)
                        usageStatsList.remove(i);
                }

                Collections.sort(usageStatsList, new Comparator.TimeInForegroundComparatorDesc());

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usageListView.setUsageStatsList(usageStatsList);

                        // Get missing icons from system
                        new IconThread(usageStatsList, usageListView.getLayoutManager(), context).start();
                    }
                });



            }
        }).start();

        return usageListView;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Log.d("ULVPA", "Moving item " + position + " to recycle bin");
        container.removeView((View) object);
        recycleViewList.add((UsageListView) object);
    }

    @Nullable
    @Override
    public abstract CharSequence getPageTitle(int position);

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
        /* TODO this is a kind of inproper way to do it
         * We are doing it anyway because our destroy and recreate process
         * makes use of recycling, which should make it somewhat as performant
         * as updating each usage list view directly.
         */
    }

    protected abstract List<SimpleUsageStat> getUsageStats(int position);
}
