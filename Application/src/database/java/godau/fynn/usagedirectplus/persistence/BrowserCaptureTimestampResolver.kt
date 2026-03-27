package godau.fynn.usagedirectplus.persistence

import android.os.SystemClock

object BrowserCaptureTimestampResolver {

    fun resolve(
        eventTime: Long,
        nowWallClockMillis: Long = System.currentTimeMillis(),
        nowUptimeMillis: Long = SystemClock.uptimeMillis()
    ): Long {
        if (eventTime <= 0L) {
            return nowWallClockMillis
        }

        if (eventTime >= MIN_REASONABLE_EPOCH_MILLIS) {
            return eventTime
        }

        val elapsedSinceEvent = nowUptimeMillis - eventTime
        return if (elapsedSinceEvent >= 0L) {
            nowWallClockMillis - elapsedSinceEvent
        } else {
            nowWallClockMillis
        }
    }

    private const val MIN_REASONABLE_EPOCH_MILLIS = 946684800000L
}
