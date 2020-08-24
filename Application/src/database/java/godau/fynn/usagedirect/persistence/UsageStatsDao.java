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

import androidx.room.*;
import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.SimpleUsageStat;

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
        return getTotalTimeUsed(
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        );
    }

    public Map<Day, Long> getTotalTimePerDay() {
        Map<Day, Long> map = new LinkedHashMap<>();
        for (Day d : getDaysStored()) {
            map.put(d, getTotalTimeUsed(d));
        }
        return map;
    }

    /**
     * Takes a list of entities and adds their values to the data already stored
     * in the database, and stores the result in the database.
     *
     * @param entities List of simple usage stats that must all be on the same day
     */
    @Transaction
    public void insertIncremental(List<SimpleUsageStat> entities) {
        if (entities.size() == 0) {
            return;
        }

        Day day = entities.get(0).getDay();

        List<SimpleUsageStat> oldUsageStats = getUsageStats(day);

        Map<String, Long> applicationMillisMap = new HashMap<>();

        for (SimpleUsageStat stat : oldUsageStats) {
            applicationMillisMap.put(stat.getApplicationId(), stat.getTimeUsed());
        }

        for (SimpleUsageStat stat : entities) {
            long millis = stat.getTimeUsed();
            if (applicationMillisMap.containsKey(stat.getApplicationId())) {
                millis += applicationMillisMap.get(stat.getApplicationId());
            }

            applicationMillisMap.put(stat.getApplicationId(), millis);
        }

        List<SimpleUsageStat> newUsageStats = new ArrayList<>();
        for (String application : applicationMillisMap.keySet()) {
            long millis = applicationMillisMap.get(application);

            newUsageStats.add(new SimpleUsageStat(day, millis, application));
        }

        insert(newUsageStats);
    }
}
