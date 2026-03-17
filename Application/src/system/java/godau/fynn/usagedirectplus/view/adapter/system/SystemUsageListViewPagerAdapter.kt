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

package godau.fynn.usagedirectplus.view.adapter.system

import androidx.appcompat.app.AppCompatActivity
import godau.fynn.usagedirectplus.Comparator
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.view.adapter.UsageListViewPagerAdapter
import godau.fynn.usagedirectplus.wrapper.Interval
import godau.fynn.usagedirectplus.wrapper.IntervalTextFormat
import godau.fynn.usagedirectplus.wrapper.UsageStatsWrapper
import java.util.Collections

class SystemUsageListViewPagerAdapter(
    private val interval: Interval,
    context: AppCompatActivity,
    private val usageStatsWrapper: UsageStatsWrapper,
    private val showLastUsed: Boolean
) : UsageListViewPagerAdapter(context) {

    private var lastUsedMap: Map<String, Long>? = null
    private var count = -1

    override fun getUsageStats(position: Int): MutableList<SimpleUsageStat> {
        val offset = count - position - 1

        val usageStats = usageStatsWrapper.getUsageStatistics(interval, offset)

        if (showLastUsed && offset == 0) {
            lastUsedMap = HashMap<String, Long>().also { map ->
                for (u in usageStats) {
                    map[u.packageName] = u.lastTimeUsed
                }
            }
        }

        val simpleUsageStats = SimpleUsageStat.asSimpleStats(usageStats)
        Collections.sort(simpleUsageStats, Comparator.TimeInForegroundComparatorDesc())
        return simpleUsageStats.toMutableList()
    }

    override fun getLastUsedMap(): Map<String, Long> {
        return if (showLastUsed) {
            lastUsedMap ?: HashMap()
        } else {
            HashMap()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return IntervalTextFormat.format(interval, count - position - 1, context.resources)
    }

    override fun getCount(): Int {
        if (count == -1) count = usageStatsWrapper.getDatasetAmount(interval)
        return count
    }

    override fun notifyDataSetChanged() {
        count = -1
        super.notifyDataSetChanged()
    }
}
