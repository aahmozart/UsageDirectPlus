package godau.fynn.usagedirectplus.browser

import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import org.junit.jupiter.api.Test

class BrowserSessionReducerTest {

    @Test
    fun `reduce opens session when nothing is currently open`() {
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Example Domain",
            url = "example.com",
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        val decision = BrowserSessionReducer.reduce(
            openSession = null,
            observation = observation,
            timestamp = 1_000L,
            closeReason = BrowserTabSession.CLOSE_REASON_SWITCHED
        )

        assertThat(decision).isEqualTo(BrowserSessionDecision.Open(observation, 1_000L))
    }

    @Test
    fun `reduce updates placeholder title instead of splitting session`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Chrome",
            url = null,
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_NONE,
            closeReason = null
        )
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Example Domain",
            url = "example.com",
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        val decision = BrowserSessionReducer.reduce(
            openSession = openSession,
            observation = observation,
            timestamp = 2_000L,
            closeReason = BrowserTabSession.CLOSE_REASON_SWITCHED
        )

        assertThat(decision).isEqualTo(BrowserSessionDecision.Update(5L, observation))
    }

    @Test
    fun `reduce keeps session open when browser emits transient placeholder metadata`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Example Domain",
            url = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Chrome",
            url = null,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_NONE
        )

        val decision = BrowserSessionReducer.reduce(
            openSession = openSession,
            observation = observation,
            timestamp = 2_000L,
            closeReason = BrowserTabSession.CLOSE_REASON_SWITCHED
        )

        assertThat(decision).isEqualTo(BrowserSessionDecision.None)
    }

    @Test
    fun `reduce closes and opens when high confidence url changes`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Example Domain",
            url = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Mozilla",
            url = "mozilla.org",
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        val decision = BrowserSessionReducer.reduce(
            openSession = openSession,
            observation = observation,
            timestamp = 3_000L,
            closeReason = BrowserTabSession.CLOSE_REASON_SWITCHED
        )

        assertThat(decision).isEqualTo(
            BrowserSessionDecision.CloseAndOpen(
                sessionId = 5L,
                closedAt = 3_000L,
                closeReason = BrowserTabSession.CLOSE_REASON_SWITCHED,
                observation = observation,
                openedAt = 3_000L
            )
        )
    }

    @Test
    fun `reduce closes open session when observation disappears`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            title = "Example Domain",
            url = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )

        val decision = BrowserSessionReducer.reduce(
            openSession = openSession,
            observation = null,
            timestamp = 4_000L,
            closeReason = BrowserTabSession.CLOSE_REASON_APP_BACKGROUND
        )

        assertThat(decision).isEqualTo(
            BrowserSessionDecision.Close(
                sessionId = 5L,
                closedAt = 4_000L,
                closeReason = BrowserTabSession.CLOSE_REASON_APP_BACKGROUND
            )
        )
    }
}
