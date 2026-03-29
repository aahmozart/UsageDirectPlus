package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hostnames",
    indices = [Index(value = ["hostname"], unique = true)]
)
data class StoredHostname(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hostname: String
)
