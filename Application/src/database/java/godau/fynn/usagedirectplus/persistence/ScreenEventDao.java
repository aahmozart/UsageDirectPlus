package godau.fynn.usagedirectplus.persistence;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScreenEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<ScreenEvent> events);

    @Query("SELECT * FROM screenEvents WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp")
    List<ScreenEvent> getByTimeRange(long start, long end);

    @Query("SELECT COUNT(*) FROM screenEvents WHERE eventType = 18 AND timestamp >= :start AND timestamp <= :end")
    int getUnlockCount(long start, long end);

    @Query("SELECT * FROM screenEvents WHERE eventType IN (17, 18) AND timestamp >= :start AND timestamp <= :end ORDER BY timestamp")
    List<ScreenEvent> getKeyguardEvents(long start, long end);
}
