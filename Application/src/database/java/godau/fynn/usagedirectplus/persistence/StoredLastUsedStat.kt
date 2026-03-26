package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity

@Entity(tableName = "lastUsed", primaryKeys = ["appId"])
data class StoredLastUsedStat(
    val appId: Long,
    val lastUsed: Long
)
