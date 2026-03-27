package godau.fynn.usagedirectplus.persistence

data class BrowserTabSession(
    val id: Long,
    val openedAt: Long,
    val closedAt: Long?,
    val applicationId: String,
    val title: String?,
    val url: String?,
    val privacyMode: Int,
    val urlConfidence: Int,
    val closeReason: Int?
) {
    companion object {
        const val PRIVACY_MODE_UNKNOWN = 0
        const val PRIVACY_MODE_STANDARD = 1
        const val PRIVACY_MODE_PRIVATE = 2

        const val URL_CONFIDENCE_NONE = 0
        const val URL_CONFIDENCE_LOW = 1
        const val URL_CONFIDENCE_HIGH = 2

        const val CLOSE_REASON_UNKNOWN = 0
        const val CLOSE_REASON_SWITCHED = 1
        const val CLOSE_REASON_APP_BACKGROUND = 2
        const val CLOSE_REASON_WINDOW_LOST = 3
        const val CLOSE_REASON_SERVICE_STOPPED = 4
    }
}
