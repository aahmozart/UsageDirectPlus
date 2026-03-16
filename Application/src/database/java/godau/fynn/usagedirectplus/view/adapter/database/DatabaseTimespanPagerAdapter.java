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

package godau.fynn.usagedirectplus.view.adapter.database;

import android.app.Activity;
import android.util.Log;
import godau.fynn.usagedirectplus.persistence.HistoryDatabase;
import godau.fynn.usagedirectplus.SimpleUsageStat;
import godau.fynn.usagedirectplus.persistence.LastUsedStat;
import godau.fynn.usagedirectplus.persistence.UsageStatsDao;
import godau.fynn.usagedirectplus.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirectplus.view.adapter.UsageListViewPagerAdapter;

import java.util.Map;

public class DatabaseTimespanPagerAdapter extends TimespanPagerAdapter {

    private SimpleUsageStat[] usageStats;
    private long[] days;
    private LastUsedStat[] lastUsedStats;
    private Map<String, Integer> colorMap;

    private DatabaseUsageListViewPagerAdapter adapter;

    public DatabaseTimespanPagerAdapter(Activity context) {
        super(context);
    }

    @Override
    public void prepare(int position) {
        Log.d("DTPA", "prepare called – reading DB");
        HistoryDatabase database = HistoryDatabase.get(context);
        UsageStatsDao usageStats = database.getUsageStatsDao();

        this.usageStats = usageStats.getUsageStats();
        days = usageStats.getDaysStored();

        this.lastUsedStats = database.getLastUsedDao().getLastUsedStats();

        colorMap = database.getAppColorDao().getAppColorMap();

        database.close();
    }

    @Override
    public UsageListViewPagerAdapter getUsageListViewPagerAdapter(int position) {
        return adapter = new DatabaseUsageListViewPagerAdapter(context, usageStats, days, lastUsedStats, colorMap);
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.setUsageStats(usageStats, days, lastUsedStats, colorMap);
        super.notifyDataSetChanged();
    }
}
