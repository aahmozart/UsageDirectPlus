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

import im.dacer.androidcharts.clockpie.ClockPieSegment;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Object representing a timespan that an application was in the foreground
 */
public class ComponentForegroundStat {
    public final long beginTime, endTime;
    public final String packageName;

    public ComponentForegroundStat(long beginTime, long endTime, String packageName) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.packageName = packageName;
    }

    /**
     * @return A clock pie segment displaying this foreground stat. Over- and underdraws
     * 10 seconds.
     */
    public ClockPieSegment asClockPieSegment() {
        LocalTime beginTime = Instant
                .ofEpochMilli(this.beginTime)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        LocalTime endTime = Instant
                .ofEpochMilli(this.endTime)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return new ClockPieSegment(
                beginTime.getHour(), beginTime.getMinute(), beginTime.getSecond() - 10,
                endTime.getHour(), endTime.getMinute(), endTime.getSecond() + 10
        );
    }
}
