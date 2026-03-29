package godau.fynn.usagedirectplus.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "browserTabSessions",
    indices = [
        Index("appId"),
        Index("openedAt"),
        Index(value = ["appId", "openedAt"]),
        Index("hostnameId")
    ]
)
data class StoredBrowserTabSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: Long,
    val hostnameId: Long,
    val openedAt: Long,
    val closedAt: Long? = null,
    val privacyMode: Int = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
    val urlConfidence: Int = BrowserTabSession.URL_CONFIDENCE_NONE,
    val closeReason: Int? = null
)
