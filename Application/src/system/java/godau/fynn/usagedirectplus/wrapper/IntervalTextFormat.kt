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

import android.content.res.Resources
import androidx.annotation.StringRes
import godau.fynn.usagedirectplus.R
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.temporal.IsoFields
import java.util.Date

abstract class IntervalTextFormat private constructor() {
    companion object {
        @JvmStatic
        fun format(interval: Interval, offset: Int, resources: Resources): String {
            return when (interval) {
                Interval.DAILY -> TextFormat.formatDay(offset, resources)
                Interval.WEEKLY -> when {
                    offset == 0 -> resources.getString(R.string.ts_this_week)
                    offset == 1 -> resources.getString(R.string.ts_last_week)
                    else -> resources.getString(
                        R.string.ts_calendar_week,
                        LocalDateTime.now().minusWeeks(offset.toLong())
                            .get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString()
                    )
                }
                Interval.MONTHLY -> formatToPattern("MMMM", interval, offset)
                Interval.YEARLY -> formatToPattern("yyyy", interval, offset)
            }
        }

        @JvmStatic
        fun formatShort(interval: Interval, offset: Int): String {
            return when (interval) {
                Interval.DAILY -> formatToPattern("E", interval, offset).substring(0, 1)
                Interval.WEEKLY -> LocalDateTime.now().minusWeeks(offset.toLong())
                    .get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toString()
                Interval.MONTHLY -> formatToPattern("MMM", interval, offset)
                Interval.YEARLY -> formatToPattern("yyyy", interval, offset)
            }
        }

        @JvmStatic
        @StringRes
        fun getIntervalName(interval: Interval): Int {
            return when (interval) {
                Interval.DAILY -> R.string.span_daily
                Interval.WEEKLY -> R.string.span_weekly
                Interval.MONTHLY -> R.string.span_monthly
                Interval.YEARLY -> R.string.span_yearly
            }
        }

        private fun formatToPattern(pattern: String, interval: Interval, offset: Int): String {
            val then = interval.backInTime(offset)
            val format = SimpleDateFormat(pattern)
            return format.format(Date(then.timeInMillis))
        }
    }
}
