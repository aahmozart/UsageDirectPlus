package godau.fynn.usagedirectplus.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScreenEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(events: List<ScreenEvent>)

    @Query("SELECT * FROM screenEvents WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp")
    fun getByTimeRange(start: Long, end: Long): List<ScreenEvent>

    @Query("SELECT COUNT(*) FROM screenEvents WHERE eventType = 18 AND timestamp >= :start AND timestamp <= :end")
    fun getUnlockCount(start: Long, end: Long): Int

    @Query("SELECT * FROM screenEvents WHERE eventType IN (17, 18) AND timestamp >= :start AND timestamp <= :end ORDER BY timestamp")
    fun getKeyguardEvents(start: Long, end: Long): List<ScreenEvent>
}
