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

package godau.fynn.usagedirect.wrapper;

import android.content.Context;
import godau.fynn.usagedirect.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class NaturalText {
    private NaturalText() {}

    public static String format(Interval interval, int offset, Context context) {
        switch (interval) {
            case DAILY:
                if (offset == 0) {
                    return context.getString(R.string.ts_today);
                } else if (offset == 1) {
                    return context.getString(R.string.ts_yesterday);
                } else {
                    Calendar then = interval.backInTime(offset);
                    SimpleDateFormat format = new SimpleDateFormat(
                            offset < 7?
                                    "EEEE" : // Weekday ("Saturday")
                                    "MMM d"  // Abbr. month and day ("Jul 11")
                    );
                    return format.format(new Date(then.getTimeInMillis()));
                }

            case WEEKLY:
                if (offset == 0) {
                    return context.getString(R.string.ts_this_week);
                } else if (offset == 1) {
                    return context.getString(R.string.ts_last_week);
                } else {
                    return context.getString(R.string.ts_weeks_ago, offset);
                }
            case MONTHLY: {
                Calendar then = interval.backInTime(offset);
                SimpleDateFormat format = new SimpleDateFormat(
                        "MMMM"
                );
                return format.format(new Date(then.getTimeInMillis()));
            }
            case YEARLY:
                Calendar then = interval.backInTime(offset);
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy"
                );
                return format.format(new Date(then.getTimeInMillis()));
            default:
                return "Span -" + offset;
        }
    }
}
