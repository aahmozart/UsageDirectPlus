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
    private Day[] days;

    public DatabaseUsageListViewPagerAdapter(Activity context, List<SimpleUsageStat> usageStats, Day[] days) {
        super(context);

        this.usageStats = usageStats;
        this.days = days;
    }

    @Override
    public int getCount() {
        return days.length;
    }

    @Override
    protected List<SimpleUsageStat> getUsageStats(int position) {
        List<SimpleUsageStat> result = new ArrayList<>();

        Day day = days[position];

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
        Day day = days[position];

        int offset = 0;

        Calendar calendar = Calendar.getInstance();
        while (calendar.get(Calendar.DAY_OF_MONTH) != day.day || calendar.get(Calendar.MONTH) != day.month || calendar.get(Calendar.YEAR) != day.year) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            offset++;
        }

        return NaturalText.format(Interval.DAILY, offset, context);
    }

    public void setUsageStats(List<SimpleUsageStat> usageStatsList, Day[] days) {
        usageStats = usageStatsList;
        this.days = days;
    }
}
