package godau.fynn.usagedirectplus.persistence

import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.browser.BrowserObservation
import godau.fynn.usagedirectplus.browser.BrowserSessionDecision
import godau.fynn.usagedirectplus.browser.BrowserSupport
import org.junit.jupiter.api.Test

class BrowserCaptureReopenDecisionTest {

    private val metadata = BrowserCaptureAccessibilityService.LastClosedSessionMetadata(
        applicationId = BrowserSupport.VANADIUM_PACKAGE,
        hostname = "example.com",
        privacyMode = BrowserTabSession.PRIVACY_MODE_STANDARD
    )

    @Test
    fun `returns None when session is already open`() {
        val openSession = BrowserTabSession(
            id = 1L,
            openedAt = 1_000L,
            closedAt = null,
            applicationId = BrowserSupport.VANADIUM_PACKAGE,
            hostname = "other.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
            closeReason = null
        )

        val decision = BrowserCaptureAccessibilityService.resolveReopenDecision(
            openSession = openSession,
            activePackage = BrowserSupport.VANADIUM_PACKAGE,
            timestamp = 2_000L,
            metadata = metadata
        )

        assertThat(decision).isEqualTo(BrowserSessionDecision.None)
    }

    @Test
    fun `returns None when no stored metadata`() {
        val decision = BrowserCaptureAccessibilityService.resolveReopenDecision(
            openSession = null,
            activePackage = BrowserSupport.VANADIUM_PACKAGE,
            timestamp = 2_000L,
            metadata = null
        )

        assertThat(decision).isEqualTo(BrowserSessionDecision.None)
    }

    @Test
    fun `returns None when app ID mismatches`() {
        val decision = BrowserCaptureAccessibilityService.resolveReopenDecision(
            openSession = null,
            activePackage = BrowserSupport.CHROME_PACKAGE,
            timestamp = 2_000L,
            metadata = metadata
        )

        assertThat(decision).isEqualTo(BrowserSessionDecision.None)
    }

    @Test
    fun `returns Open with low confidence when conditions met`() {
        val decision = BrowserCaptureAccessibilityService.resolveReopenDecision(
            openSession = null,
            activePackage = BrowserSupport.VANADIUM_PACKAGE,
            timestamp = 5_000L,
            metadata = metadata
        )

        assertThat(decision).isEqualTo(
            BrowserSessionDecision.Open(
                observation = BrowserObservation(
                    applicationId = BrowserSupport.VANADIUM_PACKAGE,
                    hostname = "example.com",
                    privacyMode = BrowserTabSession.PRIVACY_MODE_STANDARD,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_LOW
                ),
                openedAt = 5_000L
            )
        )
    }

    @Test
    fun `preserves privacy mode from closed session`() {
        val privateMetadata = metadata.copy(privacyMode = BrowserTabSession.PRIVACY_MODE_PRIVATE)

        val decision = BrowserCaptureAccessibilityService.resolveReopenDecision(
            openSession = null,
            activePackage = BrowserSupport.VANADIUM_PACKAGE,
            timestamp = 5_000L,
            metadata = privateMetadata
        )

        val open = decision as BrowserSessionDecision.Open
        assertThat(open.observation.privacyMode).isEqualTo(BrowserTabSession.PRIVACY_MODE_PRIVATE)
    }
}
