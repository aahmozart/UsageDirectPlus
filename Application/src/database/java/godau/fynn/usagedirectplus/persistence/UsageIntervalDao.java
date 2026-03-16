package godau.fynn.usagedirectplus.persistence;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Dao
public abstract class UsageIntervalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(List<UsageInterval> intervals);

    @Query("SELECT * FROM usageIntervals WHERE beginTime >= :start AND endTime <= :end ORDER BY beginTime")
    public abstract List<UsageInterval> getByTimeRange(long start, long end);

    @Query("SELECT * FROM usageIntervals WHERE applicationId = :applicationId ORDER BY beginTime")
    public abstract List<UsageInterval> getByApp(String applicationId);

    @Query("SELECT * FROM usageIntervals WHERE applicationId = :applicationId AND beginTime >= :start AND endTime <= :end ORDER BY beginTime")
    public abstract List<UsageInterval> getByAppAndTimeRange(String applicationId, long start, long end);

    @Query("SELECT MAX(endTime) FROM usageIntervals")
    public abstract long getLatestEndTime();

    @Query("SELECT * FROM usageIntervals WHERE beginTime < :rangeEnd AND endTime > :rangeStart")
    protected abstract List<UsageInterval> getOverlappingInRange(long rangeStart, long rangeEnd);

    /**
     * Inserts only intervals that do not overlap with existing intervals for the same app.
     * Existing intervals are kept because they were captured closer to the actual event
     * and have a more accurate beginTime.
     */
    @Transaction
    public void insertNonOverlapping(List<UsageInterval> intervals) {
        if (intervals.isEmpty()) {
            return;
        }

        // Compute bounding time range of the new batch
        long minBegin = Long.MAX_VALUE;
        long maxEnd = Long.MIN_VALUE;
        for (UsageInterval interval : intervals) {
            if (interval.getBeginTime() < minBegin) minBegin = interval.getBeginTime();
            if (interval.getEndTime() > maxEnd) maxEnd = interval.getEndTime();
        }

        // Fetch existing overlapping intervals in one query
        List<UsageInterval> existing = getOverlappingInRange(minBegin, maxEnd);

        // Group existing by applicationId
        Map<String, List<UsageInterval>> existingByApp = new HashMap<>();
        for (UsageInterval e : existing) {
            existingByApp.computeIfAbsent(e.getApplicationId(), k -> new ArrayList<>()).add(e);
        }

        // Filter: skip new intervals that overlap an existing interval for the same app
        List<UsageInterval> toInsert = new ArrayList<>();
        for (UsageInterval candidate : intervals) {
            List<UsageInterval> appExisting = existingByApp.get(candidate.getApplicationId());
            if (appExisting == null || !overlapsAny(candidate, appExisting)) {
                toInsert.add(candidate);
            }
        }

        if (!toInsert.isEmpty()) {
            insert(toInsert);
        }
    }

    private static boolean overlapsAny(UsageInterval candidate, List<UsageInterval> existing) {
        for (UsageInterval e : existing) {
            if (candidate.getBeginTime() < e.getEndTime() && candidate.getEndTime() > e.getBeginTime()) {
                return true;
            }
        }
        return false;
    }
}
