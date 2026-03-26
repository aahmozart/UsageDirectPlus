package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "usageIntervals",
    primaryKeys = ["beginTime", "appId"],
    indices = [Index("appId")]
)
data class StoredUsageInterval(
    val beginTime: Long,
    val endTime: Long,
    val appId: Long
)
