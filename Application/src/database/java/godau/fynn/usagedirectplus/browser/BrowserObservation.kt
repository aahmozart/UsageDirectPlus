package godau.fynn.usagedirectplus.browser

import godau.fynn.usagedirectplus.persistence.BrowserTabSession

data class BrowserObservation(
    val applicationId: String,
    val hostname: String,
    val privacyMode: Int = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
    val urlConfidence: Int = BrowserTabSession.URL_CONFIDENCE_NONE
)
