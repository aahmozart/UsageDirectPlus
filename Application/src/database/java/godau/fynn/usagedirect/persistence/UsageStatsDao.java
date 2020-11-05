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
    public abstract void insert(Collection<SimpleUsageStat> entities);

    @Query("SELECT sum(timeUsed) FROM usageStats WHERE hidden = 0")
    public abstract long getTotalTimeUsed();

    /**
     * @return The total amount of stored days, including hidden ones
     */
    @Query("SELECT count(*) FROM (SELECT DISTINCT day FROM usageStats)")
    public abstract int getDaysStoredAmount();

    /**
     * @return All days that contain visible stats
     */
    @Query("SELECT DISTINCT day FROM usageStats WHERE hidden = 0 ORDER BY day")
    public abstract long[] getDaysStored();

    /**
     * @return All visible usage stats
     */
    @Query("SELECT * FROM usageStats WHERE hidden = 0")
    public abstract SimpleUsageStat[] getUsageStats();

    /**
     * @param day Day since epoch
     * @return The total duration of all visible stats for this day
     */
    @Query("SELECT sum(timeUsed) FROM usageStats WHERE day == :day AND hidden = 0")
    public abstract long getTotalTimeUsed(long day);

    /**
     * Used only for {@link #insertIncremental(List)}
     *
     * @param day Day since epoch
     * @return All stats for this day, including hidden ones
     */
    @Query("SELECT * FROM usageStats WHERE day == :day")
    protected abstract SimpleUsageStat[] getUsageStats(long day);

    /**
     * Used only for {@link #getTotalTimePerDay()}
     *
     * @return A cursor which reads a matching of day to total visible time on this
     * day
     */
    @Query("SELECT day, sum(timeUsed) FROM usageStats WHERE hidden = 0 GROUP BY day ORDER BY day")
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

        // Add old stats to map (application name is key)
        Map<String, SimpleUsageStat> applicationStatMap = new HashMap<>();
        for (SimpleUsageStat stat : oldUsageStats) {
            applicationStatMap.put(stat.getApplicationId(), stat);
        }

        // Iterate over new stats and merge them into applicationStatMap
        for (SimpleUsageStat stat : entities) {
            String application = stat.getApplicationId();

            if (applicationStatMap.containsKey(application)) {
                // Add old and new stat and insert result

                SimpleUsageStat oldStat = applicationStatMap.get(application);

                long timeUsed = stat.getTimeUsed() + oldStat.getTimeUsed();

                applicationStatMap.put(application,
                        new SimpleUsageStat(day, timeUsed, application, oldStat.isHidden())
                );
            } else {
                // Insert new stat
                applicationStatMap.put(application, stat);
            }
        }

        insert(applicationStatMap.values());
    }
}
