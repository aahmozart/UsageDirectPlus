package godau.fynn.usagedirectplus.persistence

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import godau.fynn.usagedirectplus.SimpleUsageStat

@Dao
abstract class UsageStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(entities: Collection<SimpleUsageStat>)

    @Query("SELECT sum(timeUsed) FROM usageStats WHERE hidden = 0")
    abstract fun getTotalTimeUsed(): Long

    @Query("SELECT count(*) FROM (SELECT DISTINCT day FROM usageStats)")
    abstract fun getDaysStoredAmount(): Int

    @Query("SELECT DISTINCT day FROM usageStats WHERE hidden = 0 ORDER BY day")
    abstract fun getDaysStored(): LongArray

    @Query("SELECT min(day) FROM usageStats WHERE hidden = 0")
    abstract fun getMinimumDay(): Long

    @Query("SELECT max(day) FROM usageStats WHERE hidden = 0")
    abstract fun getMaximumDay(): Long

    @Query(
        "SELECT timeUsed, usageStats.applicationId, day, hidden " +
            "FROM usageStats " +
            "LEFT JOIN colors ON colors.applicationId = usageStats.applicationId " +
            "WHERE hidden = 0 " +
            "ORDER BY priority DESC, timeUsed DESC"
    )
    abstract fun getUsageStats(): Array<SimpleUsageStat>

    @Query("SELECT sum(timeUsed) FROM usageStats WHERE day == :day AND hidden = 0")
    abstract fun getTotalTimeUsed(day: Long): Long

    @Query("SELECT count(*) FROM usageStats WHERE hidden = 1")
    abstract fun getHiddenAmount(): Int

    @Query("UPDATE usageStats SET hidden = 0")
    abstract fun markUnhiddenAll()

    @Query("SELECT * FROM usageStats WHERE day == :day")
    protected abstract fun getUsageStats(day: Long): Array<SimpleUsageStat>

    @Query("SELECT day, sum(timeUsed) FROM usageStats WHERE hidden = 0 GROUP BY day ORDER BY day")
    protected abstract fun getTotalTimePerDayCursor(): Cursor

    @Query("SELECT applicationId, sum(timeUsed) FROM usageStats WHERE hidden = 0 GROUP BY applicationId ORDER BY sum(timeUsed) DESC")
    protected abstract fun getTotalTimePerAppCursor(): Cursor

    fun getTotalTimePerDay(): Map<Long, Long> {
        val cursor = getTotalTimePerDayCursor()
        val map = LinkedHashMap<Long, Long>()
        var last = Long.MAX_VALUE - 1
        while (cursor.moveToNext()) {
            val day = cursor.getLong(0)
            for (skipped in last + 1 until day) {
                map[skipped] = 0L
            }
            map[day] = cursor.getLong(1)
            last = day
        }
        cursor.close()
        return map
    }

    fun getTotalTimePerApp(): Map<String, Long> {
        val cursor = getTotalTimePerAppCursor()
        val map = LinkedHashMap<String, Long>()
        while (cursor.moveToNext()) {
            map[cursor.getString(0)] = cursor.getLong(1)
        }
        cursor.close()
        return map
    }

    @Transaction
    open fun insertIncremental(entities: List<SimpleUsageStat>) {
        if (entities.isEmpty()) return

        val day = entities[0].day

        val oldUsageStats = getUsageStats(day)

        val applicationStatMap = HashMap<String, SimpleUsageStat>()
        for (stat in oldUsageStats) {
            applicationStatMap[stat.applicationId] = stat
        }

        for (stat in entities) {
            val application = stat.applicationId
            val oldStat = applicationStatMap[application]
            if (oldStat != null) {
                val timeUsed = stat.timeUsed + oldStat.timeUsed
                applicationStatMap[application] =
                    SimpleUsageStat(day, timeUsed, application, oldStat.hidden)
            } else {
                applicationStatMap[application] = stat
            }
        }

        insert(applicationStatMap.values)
    }

    @Transaction
    open fun markHidden(usageStat: SimpleUsageStat) {
        insert(
            listOf(
                SimpleUsageStat(
                    usageStat.day, usageStat.timeUsed,
                    usageStat.applicationId,
                    true
                )
            )
        )
    }
}
