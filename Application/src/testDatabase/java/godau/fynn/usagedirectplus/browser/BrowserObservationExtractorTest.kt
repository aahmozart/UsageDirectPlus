package godau.fynn.usagedirectplus.browser

import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import org.junit.jupiter.api.Test

class BrowserObservationExtractorTest {

    private val extractor = BrowserObservationExtractor()

    @Test
    fun `extract parses hostname from Chromium share description`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            contentDescription = "Share link to webpage BBC Home - Breaking News. Source: www.bbc.com"
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context)

        assertThat(result).isEqualTo(
            ExtractionResult.Accepted(
                BrowserObservation(
                    applicationId = BrowserSupport.CHROME_PACKAGE,
                    hostname = "bbc.com",
                    privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                )
            )
        )
    }

    @Test
    fun `extract captures hostname from Firefox address bar url`() {
        val context = BrowserObservationContext(
            applicationId = "org.mozilla.firefox",
            eventTexts = listOf("Private browsing session"),
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            paneTitle = "Example Domain",
                            viewIdResourceName = "org.mozilla.firefox:id/mozac_browser_toolbar_title"
                        ),
                        BrowserNodeSnapshot(
                            text = "example.org/docs",
                            viewIdResourceName = "org.mozilla.firefox:id/mozac_browser_toolbar_edit_url_view",
                            isEditable = true
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context)

        assertThat(result).isEqualTo(
            ExtractionResult.Accepted(
                BrowserObservation(
                    applicationId = "org.mozilla.firefox",
                    hostname = "example.org",
                    privacyMode = BrowserTabSession.PRIVACY_MODE_PRIVATE,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                )
            )
        )
    }

    @Test
    fun `extract captures hostname from secondary browser window url bar`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            activeWindowId = 10,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 10,
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            className = "android.webkit.WebView",
                            text = "Page body content"
                        )
                    )
                ),
                BrowserWindowSnapshot(
                    id = 11,
                    isFocused = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            text = "Example Domain",
                            viewIdResourceName = "com.android.chrome:id/tab_title"
                        ),
                        BrowserNodeSnapshot(
                            text = "example.com/docs",
                            viewIdResourceName = "com.android.chrome:id/url_bar",
                            isEditable = true
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context)

        assertThat(result).isEqualTo(
            ExtractionResult.Accepted(
                BrowserObservation(
                    applicationId = BrowserSupport.CHROME_PACKAGE,
                    hostname = "example.com",
                    privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                )
            )
        )
    }

    @Test
    fun `extract rejects when only title node found without url`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            text = "Example Domain",
                            viewIdResourceName = "com.android.chrome:id/tab_title"
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context)

        assertThat(result).isInstanceOf(ExtractionResult.Rejected::class.java)
    }

    @Test
    fun `extract rejects low confidence url without strong signal`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.CHROME_PACKAGE,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            text = "example.com",
                            viewIdResourceName = "com.android.chrome:id/some_label"
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context)

        assertThat(result).isEqualTo(
            ExtractionResult.Rejected(ExtractionRejectReason.URL_LOW_CONFIDENCE_ONLY)
        )
    }

    @Test
    fun `extract returns rejected when there are no supported windows`() {
        val result = extractor.extract(
            BrowserObservationContext(
                applicationId = BrowserSupport.CHROME_PACKAGE,
                windows = emptyList()
            )
        )

        assertThat(result).isEqualTo(
            ExtractionResult.Rejected(ExtractionRejectReason.NO_SUPPORTED_WINDOWS)
        )
    }

    @Test
    fun `extract detects Vanadium incognito from NTP title text`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.VANADIUM_PACKAGE,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    title = "Vanadium: New Incognito tab",
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            viewIdResourceName = "app.vanadium.browser:id/new_tab_incognito_title",
                            text = "You've gone Incognito"
                        ),
                        BrowserNodeSnapshot(
                            text = "gg.com",
                            viewIdResourceName = "app.vanadium.browser:id/url_bar",
                            isEditable = true
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context) as ExtractionResult.Accepted

        assertThat(result.observation.privacyMode).isEqualTo(BrowserTabSession.PRIVACY_MODE_PRIVATE)
        assertThat(result.observation.hostname).isEqualTo("gg.com")
    }

    @Test
    fun `extract detects Vanadium incognito from window title`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.VANADIUM_PACKAGE,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    title = "Vanadium: New Incognito tab",
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            text = "Example Domain",
                            viewIdResourceName = "app.vanadium.browser:id/tab_title"
                        ),
                        BrowserNodeSnapshot(
                            text = "example.com",
                            viewIdResourceName = "app.vanadium.browser:id/url_bar",
                            isEditable = true
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context) as ExtractionResult.Accepted

        assertThat(result.observation.privacyMode).isEqualTo(BrowserTabSession.PRIVACY_MODE_PRIVATE)
    }

    @Test
    fun `extract detects incognito from view id containing incognito`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.VANADIUM_PACKAGE,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            viewIdResourceName = "app.vanadium.browser:id/new_tab_incognito_features",
                            text = "Vanadium won't save: Your browsing history"
                        ),
                        BrowserNodeSnapshot(
                            text = "example.com",
                            viewIdResourceName = "app.vanadium.browser:id/url_bar",
                            isEditable = true
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context) as ExtractionResult.Accepted

        assertThat(result.observation.privacyMode).isEqualTo(BrowserTabSession.PRIVACY_MODE_PRIVATE)
    }

    @Test
    fun `extract returns unknown privacy mode for normal Vanadium browsing`() {
        val context = BrowserObservationContext(
            applicationId = BrowserSupport.VANADIUM_PACKAGE,
            windows = listOf(
                BrowserWindowSnapshot(
                    id = 1,
                    title = "Vanadium: Example Domain",
                    isActive = true,
                    nodes = listOf(
                        BrowserNodeSnapshot(
                            text = "Example Domain",
                            viewIdResourceName = "app.vanadium.browser:id/tab_title"
                        ),
                        BrowserNodeSnapshot(
                            text = "example.com",
                            viewIdResourceName = "app.vanadium.browser:id/url_bar",
                            isEditable = true
                        )
                    )
                )
            )
        )

        val result = extractor.extract(context) as ExtractionResult.Accepted

        assertThat(result.observation.privacyMode).isEqualTo(BrowserTabSession.PRIVACY_MODE_UNKNOWN)
    }
}
