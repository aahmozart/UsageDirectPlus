package godau.fynn.usagedirectplus.browser

import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import java.util.Locale

object BrowserSessionReducer {

    fun reduce(
        openSession: BrowserTabSession?,
        observation: BrowserObservation?,
        timestamp: Long,
        closeReason: Int
    ): BrowserSessionDecision {
        if (observation == null) {
            return if (openSession == null) {
                BrowserSessionDecision.None
            } else {
                BrowserSessionDecision.Close(openSession.id, timestamp, closeReason)
            }
        }

        if (openSession == null) {
            return BrowserSessionDecision.Open(observation, timestamp)
        }

        if (openSession.applicationId != observation.applicationId) {
            return BrowserSessionDecision.CloseAndOpen(openSession.id, timestamp, closeReason, observation, timestamp)
        }

        if (!shouldKeepSameSession(openSession, observation)) {
            return BrowserSessionDecision.CloseAndOpen(openSession.id, timestamp, closeReason, observation, timestamp)
        }

        val mergedObservation = mergeObservation(openSession, observation)
        return if (isSameMetadata(openSession, mergedObservation)) {
            BrowserSessionDecision.None
        } else {
            BrowserSessionDecision.Update(openSession.id, mergedObservation)
        }
    }

    private fun shouldKeepSameSession(
        openSession: BrowserTabSession,
        observation: BrowserObservation
    ): Boolean {
        val existingUrl = normalizeUrl(openSession.url)
        val newUrl = normalizeUrl(observation.url)

        if (openSession.privacyMode != BrowserTabSession.PRIVACY_MODE_UNKNOWN &&
            observation.privacyMode != BrowserTabSession.PRIVACY_MODE_UNKNOWN &&
            openSession.privacyMode != observation.privacyMode
        ) {
            return false
        }

        if (openSession.urlConfidence == BrowserTabSession.URL_CONFIDENCE_HIGH &&
            observation.urlConfidence == BrowserTabSession.URL_CONFIDENCE_HIGH
        ) {
            return existingUrl == newUrl
        }

        if (isBlankOrPlaceholder(observation.title) && observation.urlConfidence < openSession.urlConfidence) {
            return true
        }

        if (equivalentTitle(openSession.title, observation.title)) {
            return true
        }

        if (isBlankOrPlaceholder(openSession.title) && !observation.title.isNullOrBlank()) {
            return true
        }

        if (openSession.title.isNullOrBlank() && !observation.title.isNullOrBlank()) {
            return true
        }

        if (openSession.url.isNullOrBlank() &&
            observation.urlConfidence > openSession.urlConfidence &&
            !observation.url.isNullOrBlank()
        ) {
            return true
        }

        return false
    }

    private fun mergeObservation(
        openSession: BrowserTabSession,
        observation: BrowserObservation
    ): BrowserObservation {
        val mergedTitle = when {
            observation.title.isNullOrBlank() -> openSession.title
            isBlankOrPlaceholder(observation.title) && !openSession.title.isNullOrBlank() -> openSession.title
            isBlankOrPlaceholder(openSession.title) -> observation.title
            equivalentTitle(openSession.title, observation.title) -> {
                if (observation.title.length >= openSession.title.orEmpty().length) {
                    observation.title
                } else {
                    openSession.title
                }
            }
            else -> observation.title
        }

        val mergedUrl = when {
            observation.urlConfidence > openSession.urlConfidence && !observation.url.isNullOrBlank() -> observation.url
            else -> openSession.url
        }

        val mergedUrlConfidence = when {
            observation.urlConfidence > openSession.urlConfidence -> observation.urlConfidence
            else -> openSession.urlConfidence
        }

        val mergedPrivacy = when {
            observation.privacyMode != BrowserTabSession.PRIVACY_MODE_UNKNOWN -> observation.privacyMode
            else -> openSession.privacyMode
        }

        return BrowserObservation(
            applicationId = observation.applicationId,
            title = mergedTitle,
            url = mergedUrl,
            privacyMode = mergedPrivacy,
            urlConfidence = mergedUrlConfidence
        )
    }

    private fun isSameMetadata(
        openSession: BrowserTabSession,
        observation: BrowserObservation
    ): Boolean {
        return normalizeText(openSession.title) == normalizeText(observation.title) &&
            normalizeUrl(openSession.url) == normalizeUrl(observation.url) &&
            openSession.privacyMode == observation.privacyMode &&
            openSession.urlConfidence == observation.urlConfidence
    }

    private fun equivalentTitle(first: String?, second: String?): Boolean {
        return normalizeText(first) == normalizeText(second)
    }

    private fun normalizeText(value: String?): String? {
        return value
            ?.replace("\\s+".toRegex(), " ")
            ?.trim()
            ?.lowercase(Locale.US)
            ?.ifBlank { null }
    }

    private fun normalizeUrl(value: String?): String? {
        return value
            ?.trim()
            ?.lowercase(Locale.US)
            ?.removePrefix("https://")
            ?.removePrefix("http://")
            ?.removePrefix("www.")
            ?.ifBlank { null }
    }

    private fun isBlankOrPlaceholder(value: String?): Boolean {
        val normalized = normalizeText(value) ?: return true
        return normalized in PLACEHOLDER_TITLES
    }

    private val PLACEHOLDER_TITLES = setOf(
        "chrome",
        "vanadium",
        "firefox",
        "new tab",
        "new private tab",
        "new incognito tab",
        "private browsing",
        "private browsing session",
        "incognito tab",
        "selected incognito tab",
        "selected tab",
        "tab"
    )
}

sealed interface BrowserSessionDecision {
    data object None : BrowserSessionDecision

    data class Open(
        val observation: BrowserObservation,
        val openedAt: Long
    ) : BrowserSessionDecision

    data class Update(
        val sessionId: Long,
        val observation: BrowserObservation
    ) : BrowserSessionDecision

    data class Close(
        val sessionId: Long,
        val closedAt: Long,
        val closeReason: Int
    ) : BrowserSessionDecision

    data class CloseAndOpen(
        val sessionId: Long,
        val closedAt: Long,
        val closeReason: Int,
        val observation: BrowserObservation,
        val openedAt: Long
    ) : BrowserSessionDecision
}
