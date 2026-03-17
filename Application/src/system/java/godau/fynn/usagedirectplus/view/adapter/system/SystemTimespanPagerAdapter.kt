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
import godau.fynn.usagedirectplus.view.adapter.TimespanPagerAdapter
import godau.fynn.usagedirectplus.view.adapter.UsageListViewPagerAdapter
import godau.fynn.usagedirectplus.wrapper.Interval
import godau.fynn.usagedirectplus.wrapper.IntervalTextFormat
import godau.fynn.usagedirectplus.wrapper.UsageStatsWrapper

class SystemTimespanPagerAdapter(
    context: AppCompatActivity,
    private val usageStatsWrapper: UsageStatsWrapper
) : TimespanPagerAdapter(context) {

    override fun prepare(position: Int) {
        // Fill cache for interval
        val interval = Interval.values()[position]
        usageStatsWrapper.getDatasetAmount(interval)
    }

    override fun getUsageListViewPagerAdapter(position: Int): UsageListViewPagerAdapter {
        return SystemUsageListViewPagerAdapter(
            Interval.values()[position], context, usageStatsWrapper, position == 0
        )
    }

    override fun getCount(): Int = Interval.values().size

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(IntervalTextFormat.getIntervalName(Interval.values()[position]))
    }
}
