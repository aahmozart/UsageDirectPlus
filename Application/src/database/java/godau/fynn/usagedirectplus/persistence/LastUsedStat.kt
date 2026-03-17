package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lastUsed")
data class LastUsedStat(
    @PrimaryKey
    @JvmField val applicationId: String,
    @JvmField val lastUsed: Long
)
