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
import androidx.annotation.Nullable;
import godau.fynn.usagedirect.persistence.SimpleUsageStat;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

import java.util.List;

public class SystemUsageListViewPagerAdapter extends UsageListViewPagerAdapter {

    private final Interval interval;
    private final UsageStatsWrapper usageStatsWrapper;

    private int count = -1;

    public SystemUsageListViewPagerAdapter(Interval interval, Activity context, UsageStatsWrapper usageStatsWrapper) {
        super(context);
        this.interval = interval;
        this.usageStatsWrapper = usageStatsWrapper;
    }


    @Override
    protected List<SimpleUsageStat> getUsageStats(int position) {
        return SimpleUsageStat.asSimpleStats(usageStatsWrapper.getUsageStatistics(interval, getCount() - position - 1));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return NaturalText.format(interval, getCount() - position - 1, context);
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
