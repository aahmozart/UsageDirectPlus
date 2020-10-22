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

import android.database.Cursor;
import androidx.room.*;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.util.*;

@Dao
public abstract class UsageStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(List<SimpleUsageStat> entities);

    @Query("SELECT sum(timeUsed) FROM usageStats")
    public abstract long getTotalTimeUsed();

    @Query("SELECT count(*) FROM (SELECT DISTINCT day FROM usageStats)")
    public abstract int getDaysStoredAmount();

    @Query("SELECT DISTINCT day FROM usageStats ORDER BY day")
    public abstract long[] getDaysStored();

    @Query("SELECT * FROM usageStats")
    public abstract SimpleUsageStat[] getUsageStats();

    @Query("SELECT sum(timeUsed) FROM usageStats WHERE day == :day")
    public abstract long getTotalTimeUsed(long day);

    @Query("SELECT * FROM usageStats WHERE day == :day")
    public abstract SimpleUsageStat[] getUsageStats(long day);

    @Query("SELECT day, sum(timeUsed) FROM usageStats GROUP BY day ORDER BY day")
    protected abstract Cursor getTotalTimePerDayCursor();

    /**
     * @return A mapping of days to the accumulated time used on that day ordered
     * by day
     */
    public Map<Long, Long> getTotalTimePerDay() {
        Cursor cursor = getTotalTimePerDayCursor();
        Map<Long, Long> map = new LinkedHashMap<>();
        while (cursor.moveToNext()) {
            map.put(cursor.getLong(0), cursor.getLong(1));
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

        long day = entities.get(0).getDay();

        SimpleUsageStat[] oldUsageStats = getUsageStats(day);

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
