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

package godau.fynn.usagedirect;

import java.util.Calendar;

/**
 * Idenitifes a day by its year, month and day.
 */
public class Day {

    public final int year, month, day;

    /**
     * From a millisecond value, construct a Day object corresponding
     * to the day that this millisecond is in
     * @param millis
     */
    public Day(long millis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public Day(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
}
