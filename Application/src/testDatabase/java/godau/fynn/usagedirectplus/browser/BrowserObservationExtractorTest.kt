package godau.fynn.usagedirectplus.browser

import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import org.junit.jupiter.api.Test

class BrowserObservationExtractorTest {

    private val extractor = BrowserObservationExtractor()

    @Test
    fun `extract parses Chromium share description into title and url`() {
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
                    title = "BBC Home - Breaking News",
                    url = "www.bbc.com",
                    privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                )
            )
        )
    }

    @Test
    fun `extract captures Firefox title and address bar url when strongly identified`() {
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
                    title = "Example Domain",
                    url = "example.org/docs",
                    privacyMode = BrowserTabSession.PRIVACY_MODE_PRIVATE,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                )
            )
        )
    }

    @Test
    fun `extract accepts title from toolbar in secondary browser window`() {
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
                    title = "Example Domain",
                    url = "example.com/docs",
                    privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                )
            )
        )
    }

    @Test
    fun `extract accepts title only observations`() {
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

        assertThat(result).isEqualTo(
            ExtractionResult.Accepted(
                BrowserObservation(
                    applicationId = BrowserSupport.CHROME_PACKAGE,
                    title = "Example Domain",
                    url = null,
                    privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                    urlConfidence = BrowserTabSession.URL_CONFIDENCE_NONE
                )
            )
        )
    }

    @Test
    fun `extract rejects low confidence url without title`() {
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
}
