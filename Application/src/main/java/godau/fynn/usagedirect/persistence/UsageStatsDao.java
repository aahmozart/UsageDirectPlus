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

import android.app.usage.UsageStats;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import godau.fynn.usagedirect.Day;

import java.util.*;

@Dao
public abstract class UsageStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(List<SimpleUsageStat> entities);

    @Query("SELECT sum(timeUsed) FROM usageStats")
    public abstract long getTotalTimeUsed();

    @Query("SELECT count(*) FROM (SELECT DISTINCT day, month, year FROM usageStats)")
    public abstract int getDaysStoredAmount();

    @Query("SELECT DISTINCT day, month, year FROM usageStats ORDER BY year, month, day")
    public abstract Day[] getDaysStored();

    @Query("SELECT * FROM usageStats")
    public abstract List<SimpleUsageStat> getUsageStats();

    @Query("SELECT sum(timeUsed) FROM usageStats WHERE day == :day AND month == :month AND year == :year")
    protected abstract int getTotalTimeUsed(int day, int month, int year);

    @Query("SELECT * FROM usageStats WHERE day == :day AND month == :month AND year == :year")
    protected abstract List<SimpleUsageStat> getUsageStats(int day, int month, int year);

    public long getTotalTimeUsed(Day day) {
        return getTotalTimeUsed(day.day, day.month, day.year);
    }

    public List<SimpleUsageStat> getUsageStats(Day day) {
        return getUsageStats(day.day, day.month, day.year);
    }

    /**
     * @param calendar A calendar set to the day you want to query
     */
    public long getTotalTimeUsed(Calendar calendar) {
        return getTotalTimeUsed(new Day(calendar.getTimeInMillis()));
    }

    public Map<Day, Long> getTotalTimePerDay() {
        Map<Day, Long> map = new LinkedHashMap<>();
        for (Day d : getDaysStored()) {
            map.put(d, getTotalTimeUsed(d));
        }
        return map;
    }
}
