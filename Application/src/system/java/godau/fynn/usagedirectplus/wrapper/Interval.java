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

package godau.fynn.usagedirectplus.wrapper;

import android.app.usage.UsageStatsManager;

import java.util.Calendar;

/**
 * Enum represents the intervals for {@link android.app.usage.UsageStatsManager} so that
 * values for intervals can be found by a String representation. Furthermore calculates
 * a timepoint somewhen in a past interval.
 */
public enum Interval {

    DAILY(UsageStatsManager.INTERVAL_DAILY, Calendar.DAY_OF_MONTH),
    WEEKLY(UsageStatsManager.INTERVAL_WEEKLY, Calendar.WEEK_OF_MONTH),
    MONTHLY(UsageStatsManager.INTERVAL_MONTHLY, Calendar.MONTH),
    YEARLY(UsageStatsManager.INTERVAL_YEARLY, Calendar.YEAR);


    final int interval;
    private final int calendarField;

    /**
     * @param interval             {@link UsageStatsManager} interval
     * @param calendarField        Duration of the interval in milliseconds
     */
    Interval(int interval, int calendarField) {
        this.interval = interval;
        this.calendarField = calendarField;
    }

    /**
     * @param times Amount of intervals to go back
     * @return A calendar that lies within the <code>times</code>-th interval back in time
     */
    public Calendar backInTime(int times) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(calendarField, -times);
        return calendar;
    }
}
