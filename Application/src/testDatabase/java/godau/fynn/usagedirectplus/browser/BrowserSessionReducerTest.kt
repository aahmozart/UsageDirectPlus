package godau.fynn.usagedirectplus.browser

import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import org.junit.jupiter.api.Test

class BrowserSessionReducerTest {

    @Test
    fun `reduce opens session when nothing is currently open`() {
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
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
    fun `reduce updates session when same hostname with new privacy mode`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_STANDARD,
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
    fun `reduce emits none when same hostname and same metadata`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
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
    fun `reduce closes and opens when hostname changes`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "mozilla.org",
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
            hostname = "example.com",
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

    @Test
    fun `reduce splits session when privacy modes conflict`() {
        val openSession = BrowserTabSession(
            id = 5L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_STANDARD,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )
        val observation = BrowserObservation(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            hostname = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_PRIVATE,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        val decision = BrowserSessionReducer.reduce(
            openSession = openSession,
            observation = observation,
            timestamp = 2_000L,
            closeReason = BrowserTabSession.CLOSE_REASON_SWITCHED
        )

        assertThat(decision).isEqualTo(
            BrowserSessionDecision.CloseAndOpen(
                sessionId = 5L,
                closedAt = 2_000L,
                closeReason = BrowserTabSession.CLOSE_REASON_SWITCHED,
                observation = observation,
                openedAt = 2_000L
            )
        )
    }
}
