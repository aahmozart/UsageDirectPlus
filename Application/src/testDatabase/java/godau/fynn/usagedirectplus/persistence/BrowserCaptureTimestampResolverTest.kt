package godau.fynn.usagedirectplus.persistence

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BrowserCaptureTimestampResolverTest {

    @Test
    fun resolveKeepsEpochEventTimes() {
        val epochTime = 1_743_046_524_604L

        val resolved = BrowserCaptureTimestampResolver.resolve(
            eventTime = epochTime,
            nowWallClockMillis = 1_800_000_000_000L,
            nowUptimeMillis = 123_456L
        )

        assertThat(resolved).isEqualTo(epochTime)
    }

    @Test
    fun resolveTranslatesUptimeEventTimesToWallClock() {
        val resolved = BrowserCaptureTimestampResolver.resolve(
            eventTime = 48_500L,
            nowWallClockMillis = 1_743_046_524_604L,
            nowUptimeMillis = 50_000L
        )

        assertThat(resolved).isEqualTo(1_743_046_523_104L)
    }

    @Test
    fun resolveFallsBackToCurrentWallClockForInvalidEventTimes() {
        val now = 1_743_046_524_604L

        val zeroTime = BrowserCaptureTimestampResolver.resolve(
            eventTime = 0L,
            nowWallClockMillis = now,
            nowUptimeMillis = 50_000L
        )
        val futureUptime = BrowserCaptureTimestampResolver.resolve(
            eventTime = 60_000L,
            nowWallClockMillis = now,
            nowUptimeMillis = 50_000L
        )

        assertThat(zeroTime).isEqualTo(now)
        assertThat(futureUptime).isEqualTo(now)
    }
}
