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
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

public class SystemTimespanPagerAdapter extends TimespanPagerAdapter {

    private final UsageStatsWrapper usageStatsWrapper;

    public SystemTimespanPagerAdapter(Activity context, UsageStatsWrapper usageStatsWrapper) {
        super(context);
        this.usageStatsWrapper = usageStatsWrapper;
    }

    @Override
    public void prepare(int position) {
        // Fill cache for interval
        final Interval interval = Interval.values()[position];
        usageStatsWrapper.getDatasetAmount(interval);
    }

    @Override
    public UsageListViewPagerAdapter getUsageListViewPagerAdapter(int position) {
        return new SystemUsageListViewPagerAdapter(Interval.values()[position], context, usageStatsWrapper);
    }

    @Override
    public int getCount() {
        return Interval.values().length;
    }
}
