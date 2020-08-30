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
import android.content.Context;
import androidx.annotation.Nullable;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseUsageListViewPagerAdapter extends UsageListViewPagerAdapter {

    private List<SimpleUsageStat> usageStats;
    private long[] days;
    private String zoneId;

    public DatabaseUsageListViewPagerAdapter(Activity context, List<SimpleUsageStat> usageStats, long[] days) {
        super(context);

        zoneId = context.getSharedPreferences("timezone", Context.MODE_PRIVATE)
                .getString("timezone",
                        ZoneId.systemDefault().getId()
                );

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

        long day = days[position];

        for (SimpleUsageStat stat : usageStats) {
            if (stat.getDay() == day) {
                result.add(stat);
            }
        }

        return result;
    }

    @Override
    protected Map<String, Long> getLastUsedMap() {
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        long day = days[position];

        long dayNow = Instant.now()
                .atZone(ZoneId.of(zoneId))
                .toLocalDate()
                .toEpochDay();

        int offset = (int) (dayNow - day);

        return NaturalText.format(Interval.DAILY, offset, context);
    }

    public void setUsageStats(List<SimpleUsageStat> usageStatsList, long[] days) {
        usageStats = usageStatsList;
        this.days = days;
    }
}
