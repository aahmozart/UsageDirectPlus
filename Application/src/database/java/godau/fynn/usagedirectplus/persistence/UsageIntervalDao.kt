package godau.fynn.usagedirectplus.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class UsageIntervalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(intervals: List<UsageInterval>)

    @Query("SELECT * FROM usageIntervals WHERE beginTime >= :start AND endTime <= :end ORDER BY beginTime")
    abstract fun getByTimeRange(start: Long, end: Long): List<UsageInterval>

    @Query("SELECT * FROM usageIntervals WHERE applicationId = :applicationId ORDER BY beginTime")
    abstract fun getByApp(applicationId: String): List<UsageInterval>

    @Query("SELECT * FROM usageIntervals WHERE applicationId = :applicationId AND beginTime >= :start AND endTime <= :end ORDER BY beginTime")
    abstract fun getByAppAndTimeRange(applicationId: String, start: Long, end: Long): List<UsageInterval>

    @Query("SELECT MAX(endTime) FROM usageIntervals")
    abstract fun getLatestEndTime(): Long

    @Query("SELECT * FROM usageIntervals WHERE beginTime < :rangeEnd AND endTime > :rangeStart")
    protected abstract fun getOverlappingInRange(rangeStart: Long, rangeEnd: Long): List<UsageInterval>

    @Transaction
    open fun insertNonOverlapping(intervals: List<UsageInterval>) {
        if (intervals.isEmpty()) return

        var minBegin = Long.MAX_VALUE
        var maxEnd = Long.MIN_VALUE
        for (interval in intervals) {
            if (interval.beginTime < minBegin) minBegin = interval.beginTime
            if (interval.endTime > maxEnd) maxEnd = interval.endTime
        }

        val existing = getOverlappingInRange(minBegin, maxEnd)

        val existingByApp = mutableMapOf<String, MutableList<UsageInterval>>()
        for (e in existing) {
            existingByApp.getOrPut(e.applicationId) { mutableListOf() }.add(e)
        }

        val toInsert = mutableListOf<UsageInterval>()
        for (candidate in intervals) {
            val appExisting = existingByApp[candidate.applicationId]
            if (appExisting == null || !overlapsAny(candidate, appExisting)) {
                toInsert.add(candidate)
            }
        }

        if (toInsert.isNotEmpty()) {
            insert(toInsert)
        }
    }

    companion object {
        private fun overlapsAny(candidate: UsageInterval, existing: List<UsageInterval>): Boolean {
            for (e in existing) {
                if (candidate.beginTime < e.endTime && candidate.endTime > e.beginTime) {
                    return true
                }
            }
            return false
        }
    }
}
