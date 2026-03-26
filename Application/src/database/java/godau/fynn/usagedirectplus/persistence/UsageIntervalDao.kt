package godau.fynn.usagedirectplus.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class UsageIntervalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertStored(intervals: List<StoredUsageInterval>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertApps(apps: List<StoredApp>)

    @Query("SELECT * FROM apps WHERE applicationId IN (:applicationIds)")
    protected abstract fun getAppsByApplicationIds(applicationIds: List<String>): List<StoredApp>

    @Query(
        "SELECT usageIntervals.beginTime AS beginTime, usageIntervals.endTime AS endTime, apps.applicationId AS applicationId " +
            "FROM usageIntervals " +
            "INNER JOIN apps ON apps.id = usageIntervals.appId " +
            "WHERE usageIntervals.beginTime < :end AND usageIntervals.endTime > :start " +
            "ORDER BY usageIntervals.beginTime"
    )
    abstract fun getByTimeRange(start: Long, end: Long): List<UsageInterval>

    @Query(
        "SELECT usageIntervals.beginTime AS beginTime, usageIntervals.endTime AS endTime, apps.applicationId AS applicationId " +
            "FROM usageIntervals " +
            "INNER JOIN apps ON apps.id = usageIntervals.appId " +
            "WHERE apps.applicationId = :applicationId " +
            "ORDER BY usageIntervals.beginTime"
    )
    abstract fun getByApp(applicationId: String): List<UsageInterval>

    @Query(
        "SELECT usageIntervals.beginTime AS beginTime, usageIntervals.endTime AS endTime, apps.applicationId AS applicationId " +
            "FROM usageIntervals " +
            "INNER JOIN apps ON apps.id = usageIntervals.appId " +
            "WHERE apps.applicationId = :applicationId AND usageIntervals.beginTime < :end AND usageIntervals.endTime > :start " +
            "ORDER BY usageIntervals.beginTime"
    )
    abstract fun getByAppAndTimeRange(applicationId: String, start: Long, end: Long): List<UsageInterval>

    @Query("SELECT MAX(endTime) FROM usageIntervals")
    abstract fun getLatestEndTime(): Long

    @Query(
        "SELECT usageIntervals.beginTime AS beginTime, usageIntervals.endTime AS endTime, apps.applicationId AS applicationId " +
            "FROM usageIntervals " +
            "INNER JOIN apps ON apps.id = usageIntervals.appId " +
            "WHERE usageIntervals.beginTime < :rangeEnd AND usageIntervals.endTime > :rangeStart"
    )
    protected abstract fun getOverlappingInRange(rangeStart: Long, rangeEnd: Long): List<UsageInterval>

    @Transaction
    open fun insertNonOverlapping(intervals: List<UsageInterval>) {
        if (intervals.isEmpty()) return

        var minBegin = Long.MAX_VALUE
        var maxEnd = Long.MIN_VALUE
        for (interval in intervals) {
            if (interval.endTime <= interval.beginTime) continue
            if (interval.beginTime < minBegin) minBegin = interval.beginTime
            if (interval.endTime > maxEnd) maxEnd = interval.endTime
        }

        if (minBegin == Long.MAX_VALUE) return

        val existing = getOverlappingInRange(minBegin, maxEnd)

        val existingByApp = mutableMapOf<String, MutableList<UsageInterval>>()
        for (e in existing) {
            existingByApp.getOrPut(e.applicationId) { mutableListOf() }.add(e)
        }

        val toInsert = mutableListOf<UsageInterval>()
        for (candidate in intervals) {
            if (candidate.endTime <= candidate.beginTime) continue
            val appExisting = existingByApp[candidate.applicationId]
            if (appExisting == null || !overlapsAny(candidate, appExisting)) {
                toInsert.add(candidate)
            }
        }

        if (toInsert.isNotEmpty()) {
            val appIds = resolveAppIds(toInsert.map(UsageInterval::applicationId))
            insertStored(
                toInsert.mapNotNull { interval ->
                    val appId = appIds[interval.applicationId] ?: return@mapNotNull null
                    StoredUsageInterval(interval.beginTime, interval.endTime, appId)
                }
            )
        }
    }

    private fun resolveAppIds(applicationIds: Collection<String>): Map<String, Long> {
        val uniqueIds = LinkedHashSet(applicationIds)
        if (uniqueIds.isEmpty()) return emptyMap()

        insertApps(uniqueIds.map { StoredApp(applicationId = it) })
        return getAppsByApplicationIds(uniqueIds.toList())
            .associate { it.applicationId to it.id }
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
