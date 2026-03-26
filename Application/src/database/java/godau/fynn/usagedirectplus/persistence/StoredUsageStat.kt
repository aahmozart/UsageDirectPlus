package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity

@Entity(tableName = "usageStats", primaryKeys = ["day", "appId"])
data class StoredUsageStat(
    val day: Long,
    val timeUsed: Long,
    val appId: Long,
    val hidden: Boolean
)
