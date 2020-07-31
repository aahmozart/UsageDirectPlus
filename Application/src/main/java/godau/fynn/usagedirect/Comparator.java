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

import android.app.usage.UsageStats;
import godau.fynn.usagedirect.persistence.SimpleUsageStat;

public class Comparator {
    /**
     * The {@link java.util.Comparator} to sort a collection of {@link UsageStats} by the timestamp
     * of the last time the app was used in descending order.
     */
    public static class LastTimeLaunchedComparatorDesc implements java.util.Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
        }
    }

    /**
     * A {@link java.util.Comparator} to sort a collection of {@link UsageStats} by total screen time.
     */
    public static class TimeInForegroundComparatorDesc implements java.util.Comparator<SimpleUsageStat> {

        @Override
        public int compare(SimpleUsageStat left, SimpleUsageStat right) {
            return Long.compare(right.getTimeUsed(), left.getTimeUsed());
        }
    }
}
