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

package godau.fynn.usagedirectplus.view.adapter.database

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.persistence.LastUsedStat
import godau.fynn.usagedirectplus.view.adapter.TimespanPagerAdapter
import godau.fynn.usagedirectplus.view.adapter.UsageListViewPagerAdapter

class DatabaseTimespanPagerAdapter(context: AppCompatActivity) : TimespanPagerAdapter(context) {

    private lateinit var usageStats: Array<SimpleUsageStat>
    private lateinit var days: LongArray
    private lateinit var lastUsedStats: Array<LastUsedStat>
    private lateinit var colorMap: Map<String, Int>

    private lateinit var adapter: DatabaseUsageListViewPagerAdapter

    override fun prepare(position: Int) {
        Log.d("DTPA", "prepare called -- reading DB")
        val database = HistoryDatabase.get(context)
        val usageStatsDao = database.getUsageStatsDao()

        usageStats = usageStatsDao.getUsageStats()
        days = usageStatsDao.getDaysStored()

        lastUsedStats = database.getLastUsedDao().getLastUsedStats()

        colorMap = database.getAppColorDao().getAppColorMap()

        database.close()
    }

    override fun getUsageListViewPagerAdapter(position: Int): UsageListViewPagerAdapter {
        adapter = DatabaseUsageListViewPagerAdapter(context, usageStats, days, lastUsedStats, colorMap)
        return adapter
    }

    override fun getCount(): Int = 1

    override fun notifyDataSetChanged() {
        adapter.setUsageStats(usageStats, days, lastUsedStats, colorMap)
        super.notifyDataSetChanged()
    }
}
