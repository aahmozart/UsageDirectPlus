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

package godau.fynn.usagedirectplus.view.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import godau.fynn.usagedirectplus.Comparator;
import godau.fynn.usagedirectplus.thread.icon.AppUsageStatisticsIconThread;
import godau.fynn.usagedirectplus.thread.icon.IconThread;
import godau.fynn.usagedirectplus.SimpleUsageStat;
import godau.fynn.usagedirectplus.view.UsageListView;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Child pager of {@link TimespanPagerAdapter}, pages {@link UsageListView}s
 */
public abstract class UsageListViewPagerAdapter extends PagerAdapter {
    private static final Queue<UsageListView> recycleViewList = new LinkedBlockingQueue<>();
    protected final Activity context;

    /**
     * For performance, don't instantiate the first two pages if they are not actually displayed.
     *
     * @see <a href="https://codeberg.org/fynngodau/usageDirect/issues/55">related issue</a>
     */
    private boolean fakeInstantiateFirstPages = true;


    public UsageListViewPagerAdapter(Activity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        // "This does not need to be a View[…]"
        if (fakeInstantiateFirstPages && position < 2 && getCount() > 7) {
            Log.d("CPVPA", "Faking item at position " + position);
            return new Object();
        }

        if (position > 5) fakeInstantiateFirstPages = false;

        // Setup view

        final UsageListView usageListView;
        if (recycleViewList.peek() == null) {
            usageListView = new UsageListView(context);
            Log.d("ULVPA", "Creating usage list view for position " + position);
        } else {
            Log.d("ULVPA", "Recycling usage list view from recycle bin for position " + position);
            usageListView = recycleViewList.poll();
            usageListView.setUsageStatsList(null);
        }
        container.addView(usageListView);

        usageListView.setOnItemClickListener(getOnItemClickListener());

        // Get data

        new Thread(() -> {

            final List<SimpleUsageStat> usageStatsList = getUsageStats(position);

            // Filter unused apps
            for (int i = usageStatsList.size() - 1; i >= 0; i--) {
                SimpleUsageStat usageStat = usageStatsList.get(i);
                if (usageStat.getTimeUsed() <= 0)
                    usageStatsList.remove(i);
            }

            final Map<String, Long> lastUsedMap = getLastUsedMap();
            final Map<String, Integer> colorMap = getColorMap();

            context.runOnUiThread(() -> {
                usageListView.setLastUsedMap(lastUsedMap);
                usageListView.setUsageStatsList(usageStatsList);
                usageListView.setColorMap(colorMap);

                // Get missing icons from system
                new AppUsageStatisticsIconThread(
                        usageStatsList, usageListView.getLayoutManager(), context
                ).start();
            });



        }).start();

        return usageListView;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object instanceof View) {
            Log.d("ULVPA", "Moving item " + position + " to recycle bin");
            container.removeView((View) object);
            recycleViewList.add((UsageListView) object);
        } // Discard placeholder fake items
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

    /**
     * Usage stats must be pre-sorted globally by usage time (and priority, if applicable)
     *
     * TODO Sorting globally is not needed and takes additional time.
     */
    protected abstract List<SimpleUsageStat> getUsageStats(int position);

    /**
     * Called after {@link #getUsageStats(int)}
     *
     * @return A mapping of package names to last used timestamp
     */
    protected abstract @NonNull Map<String, Long> getLastUsedMap();

    protected @NonNull Map<String, Integer> getColorMap() {
        return new HashMap<>();
    }

    protected UsageListAdapter.OnItemClickListener getOnItemClickListener() {
        return null;
    }
}
