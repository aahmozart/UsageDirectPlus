package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity

@Entity(tableName = "colors", primaryKeys = ["appId"])
data class StoredAppColor(
    val appId: Long,
    val color: Int,
    val priority: Int
)
