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
import android.util.Log;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.persistence.UsageStatsDao;
import godau.fynn.usagedirect.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;

import java.util.List;

public class DatabaseTimespanPagerAdapter extends TimespanPagerAdapter {

    private List<SimpleUsageStat> usageStatsList;
    private long[] days;
    private DatabaseUsageListViewPagerAdapter adapter;

    public DatabaseTimespanPagerAdapter(Activity context) {
        super(context);
    }

    @Override
    public void prepare(int position) {
        Log.d("DTPA", "prepare called – reading DB");
        HistoryDatabase database = HistoryDatabase.get(context);
        UsageStatsDao usageStats = database.getUsageStatsDao();

        usageStatsList = usageStats.getUsageStats();
        days = usageStats.getDaysStored();

        database.close();
    }

    @Override
    public UsageListViewPagerAdapter getUsageListViewPagerAdapter(int position) {
        return adapter = new DatabaseUsageListViewPagerAdapter(context, usageStatsList, days);
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.setUsageStats(usageStatsList, days);
        super.notifyDataSetChanged();
    }
}
