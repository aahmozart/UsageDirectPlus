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

package godau.fynn.usagedirect.view.adapter.system;

import android.app.Activity;
import android.app.usage.UsageStats;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import godau.fynn.usagedirect.Comparator;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.IntervalTextFormat;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

import java.util.*;

public class SystemUsageListViewPagerAdapter extends UsageListViewPagerAdapter {

    private final Interval interval;
    private final UsageStatsWrapper usageStatsWrapper;

    private Map<String, Long> lastUsedMap;

    private int count = -1;

    private final boolean showLastUsed;

    public SystemUsageListViewPagerAdapter(Interval interval, Activity context, UsageStatsWrapper usageStatsWrapper, boolean showLastUsed) {
        super(context);
        this.interval = interval;
        this.usageStatsWrapper = usageStatsWrapper;
        this.showLastUsed = showLastUsed;
    }


    @Override
    protected List<SimpleUsageStat> getUsageStats(int position) {
        int offset = getCount() - position - 1;

        List<UsageStats> usageStats = usageStatsWrapper.getUsageStatistics(interval, offset);

        if (showLastUsed && offset == 0) {
            lastUsedMap = new HashMap<>();

            for (UsageStats u : usageStats) {
                lastUsedMap.put(u.getPackageName(), u.getLastTimeUsed());
            }
        }

        List<SimpleUsageStat> simpleUsageStats = SimpleUsageStat.asSimpleStats(usageStats);
        Collections.sort(simpleUsageStats, new Comparator.TimeInForegroundComparatorDesc());
        return simpleUsageStats;
    }

    @NonNull
    @Override
    protected Map<String, Long> getLastUsedMap() {
        if (showLastUsed) {
            return lastUsedMap;
        } else {
            return new HashMap<>();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return IntervalTextFormat.format(interval, getCount() - position - 1, context.getResources());
    }

    @Override
    public int getCount() {
        if (count == -1) count = usageStatsWrapper.getDatasetAmount(interval);
        return count;
    }

    @Override
    public void notifyDataSetChanged() {
        count = -1;
        super.notifyDataSetChanged();
    }
}
