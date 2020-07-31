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

package godau.fynn.usagedirect.view.adapter.database;

import android.app.Activity;
import androidx.annotation.Nullable;
import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseUsageListViewPagerAdapter extends UsageListViewPagerAdapter {

    private List<SimpleUsageStat> usageStats;
    private final int count;

    public DatabaseUsageListViewPagerAdapter(Activity context, List<SimpleUsageStat> usageStats, int dayCount) {
        super(context);

        this.usageStats = usageStats;
        this.count = dayCount;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    protected List<SimpleUsageStat> getUsageStats(int position) {
        List<SimpleUsageStat> result = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -(getCount() - position - 1));

        Day day = new Day(calendar.getTimeInMillis());

        for (SimpleUsageStat stat : usageStats) {
            if (stat.getDay().equals(day)) {
                result.add(stat);
            }
        }

        return result;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return NaturalText.format(Interval.DAILY, getCount() - position - 1, context);
    }

    public void setUsageStatsList(List<SimpleUsageStat> usageStatsList) {
        usageStats = usageStatsList;
    }
}
