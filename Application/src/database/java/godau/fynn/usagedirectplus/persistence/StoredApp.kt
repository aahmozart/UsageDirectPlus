package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "apps",
    indices = [Index(value = ["applicationId"], unique = true)]
)
data class StoredApp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val applicationId: String
)
