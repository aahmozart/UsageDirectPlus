package godau.fynn.usagedirectplus

import android.app.usage.UsageStats
import androidx.room.Entity
import androidx.room.Ignore
import java.time.Instant
import java.time.ZoneId

@Entity(tableName = "usageStats", primaryKeys = ["day", "applicationId"])
open class SimpleUsageStat(
    val day: Long,
    val timeUsed: Long,
    val applicationId: String,
    val hidden: Boolean
) {
    @Ignore
    constructor(day: Long, timeUsed: Long, applicationId: String) : this(day, timeUsed, applicationId, false)

    @Ignore
    constructor(systemUsageStat: UsageStats) : this(
        Instant.ofEpochMilli(systemUsageStat.lastTimeUsed)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay(),
        systemUsageStat.totalTimeInForeground,
        systemUsageStat.packageName,
        false
    )

    companion object {
        @JvmStatic
        fun asSimpleStats(usageStats: List<UsageStats>): List<SimpleUsageStat> {
            return usageStats.map { SimpleUsageStat(it) }
        }
    }
}
