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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.activity.UsageIntervalsActivity
import godau.fynn.usagedirectplus.persistence.LastUsedStat
import godau.fynn.usagedirectplus.view.adapter.UsageListAdapter
import godau.fynn.usagedirectplus.view.adapter.UsageListViewPagerAdapter
import godau.fynn.usagedirectplus.wrapper.TextFormat
import java.time.LocalDate

class DatabaseUsageListViewPagerAdapter(
    context: AppCompatActivity,
    private var usageStats: Array<SimpleUsageStat>,
    private var days: LongArray,
    private var lastUsedStats: Array<LastUsedStat>,
    private var colorMap: Map<String, Int>
) : UsageListViewPagerAdapter(context) {

    override fun getCount(): Int = days.size

    override fun getUsageStats(position: Int): MutableList<SimpleUsageStat> {
        val result = mutableListOf<SimpleUsageStat>()

        val day = days[position]

        for (stat in usageStats) {
            if (stat.day == day) {
                result.add(stat)
            }
        }

        return result
    }

    override fun getLastUsedMap(): Map<String, Long> {
        val applicationLastUsedMap = HashMap<String, Long>()

        for (lastUsedStat in lastUsedStats) {
            applicationLastUsedMap[lastUsedStat.applicationId] = lastUsedStat.lastUsed
        }

        return applicationLastUsedMap
    }

    override fun getColorMap(): Map<String, Int> {
        return colorMap
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val day = days[position]

        val dayNow = LocalDate.now().toEpochDay()

        val offset = (dayNow - day).toInt()

        return TextFormat.formatDay(offset, context.resources).replace(' ', '\n')
    }

    override fun getOnItemClickListener(): UsageListAdapter.OnItemClickListener {
        return UsageListAdapter.OnItemClickListener { stat ->
            val intent = Intent(context, UsageIntervalsActivity::class.java)
            intent.putExtra("applicationId", stat.applicationId)
            intent.putExtra("day", stat.day)
            context.startActivity(intent)
        }
    }

    fun setUsageStats(
        usageStats: Array<SimpleUsageStat>,
        days: LongArray,
        lastUsedStats: Array<LastUsedStat>,
        colorMap: Map<String, Int>
    ) {
        this.usageStats = usageStats
        this.days = days
        this.lastUsedStats = lastUsedStats
        this.colorMap = colorMap
    }
}
