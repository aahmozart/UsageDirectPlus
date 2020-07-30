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

package godau.fynn.usagedirect.persistence;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import godau.fynn.usagedirect.Day;

/**
 * Similar to UsageStats, but contains less data and is stored in the
 * {@link HistoryDatabase}
 */
@Entity(tableName = "usageStats", primaryKeys = {"day", "applicationId"})
public class SimpleUsageStat {

    @Embedded @NonNull
    private final Day day;

    private final long timeUsed;

    private final @NonNull
    String applicationId;

    public SimpleUsageStat(@NonNull Day day, long timeUsed, @NonNull String applicationId) {
        this.day = day;
        this.timeUsed = timeUsed;
        this.applicationId = applicationId;
    }

    public @NonNull Day getDay() {
        return day;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public @NonNull String getApplicationId() {
        return applicationId;
    }
}
