package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screenEvents",
    indices = [Index("timestamp")]
)
data class ScreenEvent(
    @PrimaryKey
    val timestamp: Long,
    val eventType: Int
) {
    companion object {
        const val SCREEN_ON = 15
        const val SCREEN_OFF = 16
        const val KEYGUARD_SHOWN = 17
        const val KEYGUARD_HIDDEN = 18
    }
}
