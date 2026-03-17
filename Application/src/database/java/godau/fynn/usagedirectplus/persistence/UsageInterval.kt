package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "usageIntervals",
    primaryKeys = ["beginTime", "applicationId"],
    indices = [Index("applicationId"), Index("beginTime")]
)
data class UsageInterval(
    val beginTime: Long,
    val endTime: Long,
    val applicationId: String
)
