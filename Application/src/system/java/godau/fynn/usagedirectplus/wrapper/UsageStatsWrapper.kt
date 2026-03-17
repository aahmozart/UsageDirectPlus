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

package godau.fynn.usagedirectplus.wrapper

import android.app.usage.UsageStats
import android.content.Context
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap

/**
 * Wrapper class for `queryUsageStats(...)` calls to the UsageStatsManager class
 *
 * Uses a `static` cache to speed up requests. If you want to clear the cache,
 * call [flushCache].
 */
class UsageStatsWrapper(context: Context) : UsageStatsManagerWrapper(context) {

    /**
     * Assumes usage stats permission is granted, check beforehand using
     * [isPermissionGranted].
     *
     * Uses a local cache to be speed up requests and thus does not guarantee
     * live data
     *
     * @param interval The time interval by which the stats are aggregated.
     * @param offset   Amount of intervals to go back in time
     * @return A list of [UsageStats].
     */
    fun getUsageStatistics(interval: Interval, offset: Int): List<UsageStats> {
        val hash = Objects.hash(interval, offset).toLong()
        return if (cache.containsKey(hash)) {
            cache[hash]!!
        } else {
            val endTime = interval.backInTime(offset).timeInMillis
            val beginTime = endTime - 60000

            val usageStats = usageStatsManager!!.queryUsageStats(interval.interval, beginTime, endTime)
            cache[hash] = usageStats
            usageStats
        }
    }

    /**
     * Accumulate UsageStatistics of a period
     * @see getUsageStatistics
     * @return A time value in seconds
     */
    fun getAccumulatedTime(interval: Interval, offset: Int): Int {
        val usageStats = getUsageStatistics(interval, offset)

        var sum = 0
        for (stats in usageStats) {
            sum += (stats.totalTimeInForeground / 1000).toInt()
        }

        return sum
    }

    /**
     * Accumulates UsageStatistics of multiple days
     * @param intervals How many intervals back in time should be added to the list
     * @return A chronologically ordered list of time values in seconds, containing
     *         `intervals + 1` items
     */
    fun getAccumulatedTimes(interval: Interval, intervals: Int): ArrayList<Int> {
        val accumulation = ArrayList<Int>()

        for (i in intervals downTo 0) {
            accumulation.add(getAccumulatedTime(interval, i))
        }

        return accumulation
    }

    /**
     * Incrementally tests intervals further in the past to find out the total amount
     * of intervals that have data associated with them.
     *
     * Take care, this method has **bad performance**.
     *
     * @return Amount of intervals with a corresponding dataset
     */
    fun getDatasetAmount(interval: Interval): Int {
        var stats: List<UsageStats>
        var amount = 0
        do {
            stats = getUsageStatistics(interval, amount++)
        } while (stats.isNotEmpty())
        return --amount
    }

    companion object {
        private var cache: MutableMap<Long, List<UsageStats>> = ConcurrentHashMap()

        /**
         * Clears cache
         */
        @JvmStatic
        fun flushCache() {
            cache = ConcurrentHashMap()
        }
    }
}
