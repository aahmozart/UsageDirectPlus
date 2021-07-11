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
     * @return The total amount of stored days, including ones where all entries
     * are hidden
     */
    @Query("SELECT count(*) FROM (SELECT DISTINCT day FROM usageStats)")
    public abstract int getDaysStoredAmount();

    /**
     * @return All days that contain visible stats
     */
    @Query("SELECT DISTINCT day FROM usageStats WHERE hidden = 0 ORDER BY day")
    public abstract long[] getDaysStored();

    @Query("SELECT min(day) FROM usageStats WHERE hidden = 0")
    public abstract long getMinimumDay();

    @Query("SELECT max(day) FROM usageStats WHERE hidden = 0")
    public abstract long getMaximumDay();

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
     * @return Number of deleted entries
     */
    @Query("SELECT count(*) FROM usageStats WHERE hidden = 1")
    public abstract int getHiddenAmount();

    /**
     * Mark every row as "not hidden"
     */
    @Query("UPDATE usageStats SET hidden = 0")
    public abstract void markUnhiddenAll();

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
     * Used only for {@link #getTotalTimePerApp()}
     *
     * @return A cursor which reads a matching of application ID to its total visible time
     */
    @Query("SELECT applicationId, sum(timeUsed) FROM usageStats WHERE hidden = 0 GROUP BY applicationId ORDER BY sum(timeUsed) DESC")
    protected abstract Cursor getTotalTimePerAppCursor();

    /**
     * @return A mapping of days to the accumulated time used on that day ordered
     * by day, including days without stats in between minimum and maximum day
     */
    public Map<Long, Long> getTotalTimePerDay() {
        Cursor cursor = getTotalTimePerDayCursor();
        Map<Long, Long> map = new LinkedHashMap<>();

        long last = Long.MAX_VALUE - 1;

        // Cursor starts before first row
        while (cursor.moveToNext()) {

            long day = cursor.getLong(0);

            // Insert 0 for skipped days
            for (long skipped = last + 1; skipped < day; skipped++) {
                map.put(skipped, 0L);
            }

            map.put(day, cursor.getLong(1));

            last = day;
        }

        cursor.close();
        return map;
    }

    /**
     * @return A mapping of apps to the accumulated time they were used individually
     * in milliseconds ordered by total usage time (descending)
     */
    public Map<String, Long> getTotalTimePerApp() {
        Cursor cursor = getTotalTimePerAppCursor();
        Map<String, Long> map = new LinkedHashMap<>();

        // Cursor starts before first row
        while (cursor.moveToNext()) {
            map.put(cursor.getString(0), cursor.getLong(1));
        }

        cursor.close();
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

    /**
     * Marks the given usage stat as deleted in the database.
     */
    @Transaction
    public void markHidden(SimpleUsageStat usageStat) {
        insert(Collections.singleton(
                new SimpleUsageStat(
                        usageStat.getDay(), usageStat.getTimeUsed(),
                        usageStat.getApplicationId(),
                        true
                )
        ));
    }
}
