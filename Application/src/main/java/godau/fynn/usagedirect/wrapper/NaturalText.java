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
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import godau.fynn.usagedirect.R;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
                    return formatToPattern(offset < 7?
                                    "EEEE" : // Weekday ("Saturday")
                                    "MMM d",  // Abbr. month and day ("Jul 11")
                            interval, offset);
                }

            case WEEKLY:
                if (offset == 0) {
                    return context.getString(R.string.ts_this_week);
                } else if (offset == 1) {
                    return context.getString(R.string.ts_last_week);
                } else {
                    return context.getString(R.string.ts_calendar_week, formatToPattern("w", interval, offset));
                }
            case MONTHLY:
                return formatToPattern("MMMM", interval, offset);
            case YEARLY:
                return formatToPattern("yyyy", interval, offset);
            default:
                return "Span -" + offset;
        }
    }

    public static String formatShort(Interval interval, int offset) {

        switch (interval) {
            case DAILY:
                return formatToPattern("E", interval, offset).substring(0, 1);
            case WEEKLY:
                return formatToPattern("w", interval, offset);
            case MONTHLY:
                return formatToPattern("MMM", interval, offset);
            case YEARLY:
                return formatToPattern("yyyy", interval, offset);
            default:
                throw new IllegalArgumentException("Unexpected value: " + interval);
        }
    }

    public static String formatWeekday(int weekday) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, weekday);
        return new SimpleDateFormat("E").format(new Date(c.getTimeInMillis()));
    }

    public static String formatWeekday(DayOfWeek weekday) {
        return weekday.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
    }

    public static @NonNull
    @StringRes Integer getIntervalName(Interval interval) {
        switch (interval) {
            case DAILY:
                return R.string.span_daily;
            case WEEKLY:
                return R.string.span_weekly;
            case MONTHLY:
                return R.string.span_monthly;
            case YEARLY:
                return R.string.span_yearly;
            default:
                return null;
        }
    }

    private static String formatToPattern(String pattern, Interval interval, int offset) {
        Calendar then = interval.backInTime(offset);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date(then.getTimeInMillis()));
    }
}
