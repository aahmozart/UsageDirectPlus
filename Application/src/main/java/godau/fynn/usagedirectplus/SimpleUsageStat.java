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

package godau.fynn.usagedirectplus;

import android.app.usage.UsageStats;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Similar to UsageStats, but contains less data and is stored in the
 * {@link godau.fynn.usagedirectplus.persistence.HistoryDatabase}
 */
@Entity(tableName = "usageStats", primaryKeys = {"day", "applicationId"})
public class SimpleUsageStat {

    /**
     * Days since epoch
     */
    private final long day;

    private final long timeUsed;

    private final @NonNull
    String applicationId;

    /**
     * Hidden usage stats may be stored in database
     */
    private final boolean hidden;

    @Ignore
    public SimpleUsageStat(long day, long timeUsed, @NonNull String applicationId) {
        this(day, timeUsed, applicationId, false);
    }

    public SimpleUsageStat(long day, long timeUsed, @NonNull String applicationId, boolean hidden) {
        this.day = day;
        this.timeUsed = timeUsed;
        this.applicationId = applicationId;
        this.hidden = hidden;
    }

    public SimpleUsageStat(UsageStats systemUsageStat) {
        timeUsed = systemUsageStat.getTotalTimeInForeground();
        applicationId = systemUsageStat.getPackageName();
        day = Instant.ofEpochMilli(systemUsageStat.getLastTimeUsed())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay();
        hidden = false;
    }

    /**
     * @return The day since epoch that this object concerns
     */
    public long getDay() {
        return day;
    }

    /**
     * @return The time that the application has been in the foreground in milliseconds on this day
     */
    public long getTimeUsed() {
        return timeUsed;
    }

    /**
     * @return The package name of the application that this object concerns
     */
    public @NonNull String getApplicationId() {
        return applicationId;
    }

    public boolean isHidden() {
        return hidden;
    }

    public static List<SimpleUsageStat> asSimpleStats(List<UsageStats> usageStats) {
        List<SimpleUsageStat> result = new ArrayList<>();
        for (UsageStats usageStat : usageStats) {
            result.add(new SimpleUsageStat(usageStat));
        }
        return result;
    }
}
