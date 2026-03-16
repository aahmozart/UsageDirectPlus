package godau.fynn.usagedirectplus.persistence;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageIntervalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<UsageInterval> intervals);

    @Query("SELECT * FROM usageIntervals WHERE beginTime >= :start AND endTime <= :end ORDER BY beginTime")
    List<UsageInterval> getByTimeRange(long start, long end);

    @Query("SELECT * FROM usageIntervals WHERE applicationId = :applicationId ORDER BY beginTime")
    List<UsageInterval> getByApp(String applicationId);

    @Query("SELECT * FROM usageIntervals WHERE applicationId = :applicationId AND beginTime >= :start AND endTime <= :end ORDER BY beginTime")
    List<UsageInterval> getByAppAndTimeRange(String applicationId, long start, long end);

    @Query("SELECT MAX(endTime) FROM usageIntervals")
    long getLatestEndTime();
}
