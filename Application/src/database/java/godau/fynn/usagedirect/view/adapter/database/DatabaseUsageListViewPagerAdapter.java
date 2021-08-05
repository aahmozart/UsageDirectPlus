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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.persistence.LastUsedStat;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.wrapper.TextFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUsageListViewPagerAdapter extends UsageListViewPagerAdapter {

    private SimpleUsageStat[] usageStats;
    private long[] days;
    private LastUsedStat[] lastUsedStats;
    private Map<String, Integer> colorMap;

    public DatabaseUsageListViewPagerAdapter(Activity context, SimpleUsageStat[] usageStats, long[] days,
                                             LastUsedStat[] lastUsedStats, Map<String, Integer> colorMap) {
        super(context);

        this.usageStats = usageStats;
        this.days = days;
        this.lastUsedStats = lastUsedStats;
        this.colorMap = colorMap;
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

    @NonNull
    @Override
    protected Map<String, Long> getLastUsedMap() {

        Map<String, Long> applicationLastUsedMap = new HashMap<>();

        for (LastUsedStat lastUsedStat : lastUsedStats) {
            applicationLastUsedMap.put(lastUsedStat.applicationId, lastUsedStat.lastUsed);
        }

        return applicationLastUsedMap;
    }

    @NonNull
    @Override
    protected Map<String, Integer> getColorMap() {
        return colorMap;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        long day = days[position];

        long dayNow = LocalDate.now()
                .toEpochDay();

        int offset = (int) (dayNow - day);

        return TextFormat.formatDay(offset, context.getResources()).replace(' ', '\n');
    }

    public void setUsageStats(SimpleUsageStat[] usageStats, long[] days, LastUsedStat[] lastUsedStats) {
        this.usageStats = usageStats;
        this.days = days;
        this.lastUsedStats = lastUsedStats;
    }
}
