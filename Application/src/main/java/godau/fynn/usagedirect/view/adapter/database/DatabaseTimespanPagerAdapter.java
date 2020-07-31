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
import androidx.room.Room;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.persistence.UsageStatsDao;
import godau.fynn.usagedirect.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;

import java.util.List;

public class DatabaseTimespanPagerAdapter extends TimespanPagerAdapter {

    private List<SimpleUsageStat> usageStatsList;
    private int dayCount;
    private DatabaseUsageListViewPagerAdapter adapter;

    public DatabaseTimespanPagerAdapter(Activity context) {
        super(context);
    }

    @Override
    public void prepare(int position) {
        Log.d("DTPA", "prepreare called – reading DB");
        HistoryDatabase database = Room.databaseBuilder(context, HistoryDatabase.class, HistoryDatabase.DATABASE_NAME).build();
        UsageStatsDao usageStats = database.getUsageStatsDao();

        usageStatsList = usageStats.getUsageStats();
        dayCount = usageStats.getDaysStoredAmount();
    }

    @Override
    public UsageListViewPagerAdapter getUsageListViewPagerAdapter(int position) {
        return adapter = new DatabaseUsageListViewPagerAdapter(context, usageStatsList, dayCount);
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.setUsageStatsList(usageStatsList);
        super.notifyDataSetChanged();
    }
}
