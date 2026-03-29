package godau.fynn.usagedirectplus.browser

import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import java.util.Locale
import java.util.regex.Pattern

class BrowserObservationExtractor {

    fun extract(context: BrowserObservationContext): ExtractionResult {
        val family = BrowserSupport.familyFor(context.applicationId)
            ?: return ExtractionResult.Rejected(ExtractionRejectReason.NO_SUPPORTED_WINDOWS)

        if (context.windows.isEmpty()) {
            return ExtractionResult.Rejected(ExtractionRejectReason.NO_SUPPORTED_WINDOWS)
        }

        if (!context.hasVisibleNodes()) {
            return ExtractionResult.Rejected(ExtractionRejectReason.NO_VISIBLE_NODES)
        }

        var privacyMode = collectEventSignals(context, family)
        var strongUrl: String? = null
        var sawLowConfidenceUrl = false
        var sawToolbarSignal = false

        orderedWindows(context).forEachIndexed { _, window ->
            window.title?.let { title ->
                privacyMode = maxPrivacyMode(privacyMode, detectPrivacyMode(title, family))
            }

            window.nodes.forEachIndexed { _, node ->
                if (!node.isVisibleToUser) {
                    return@forEachIndexed
                }

                if (isToolbarSignal(node)) {
                    sawToolbarSignal = true
                }

                if (family == BrowserFamily.CHROMIUM) {
                    privacyMode = maxPrivacyMode(privacyMode, detectPrivacyModeFromViewId(node))
                }

                val values = listOfNotNull(
                    node.text?.let { CandidateString(it, CandidateSource.NODE_TEXT) },
                    node.contentDescription?.let { CandidateString(it, CandidateSource.NODE_CONTENT_DESCRIPTION) },
                    node.paneTitle?.let { CandidateString(it, CandidateSource.NODE_PANE_TITLE) }
                )

                for (value in values) {
                    privacyMode = maxPrivacyMode(privacyMode, detectPrivacyMode(value.value, family))

                    parseShareUrl(value.value)?.let { shareUrl ->
                        normalizeUrl(shareUrl)?.let { strongUrl = it }
                    }

                    val normalizedUrl = normalizeUrl(value.value)
                    if (normalizedUrl != null) {
                        when (scoreUrl(node, value, family)) {
                            BrowserTabSession.URL_CONFIDENCE_HIGH -> strongUrl = normalizedUrl
                            BrowserTabSession.URL_CONFIDENCE_LOW -> sawLowConfidenceUrl = true
                        }
                    }
                }
            }
        }

        val hostname = strongUrl?.let { HostnameExtractor.extractHostname(it) }

        if (hostname == null) {
            return when {
                sawLowConfidenceUrl -> ExtractionResult.Rejected(ExtractionRejectReason.URL_LOW_CONFIDENCE_ONLY)
                !sawToolbarSignal -> ExtractionResult.Rejected(ExtractionRejectReason.TOOLBAR_NOT_FOUND)
                else -> ExtractionResult.Rejected(ExtractionRejectReason.NO_HOSTNAME)
            }
        }

        return ExtractionResult.Accepted(
            BrowserObservation(
                applicationId = context.applicationId,
                hostname = hostname,
                privacyMode = privacyMode,
                urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
            )
        )
    }

    private fun collectEventSignals(
        context: BrowserObservationContext,
        family: BrowserFamily
    ): Int {
        var privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN

        context.eventContentDescription?.let { value ->
            privacyMode = maxPrivacyMode(privacyMode, detectPrivacyMode(value, family))
        }

        context.eventTexts.forEach { value ->
            privacyMode = maxPrivacyMode(privacyMode, detectPrivacyMode(value, family))
        }

        return privacyMode
    }

    private fun orderedWindows(context: BrowserObservationContext): List<BrowserWindowSnapshot> {
        return context.windows.sortedByDescending { window ->
            when {
                window.id != null && window.id == context.activeWindowId -> 4
                window.isActive -> 3
                window.isFocused -> 2
                else -> 1
            }
        }
    }

    private fun scoreUrl(
        node: BrowserNodeSnapshot,
        value: CandidateString,
        family: BrowserFamily
    ): Int {
        val viewId = node.viewIdResourceName?.lowercase(Locale.US).orEmpty()

        var score = 0
        if (node.isEditable) score += 3
        if ("url" in viewId || "address" in viewId || "location" in viewId || "omnibox" in viewId) score += 4
        if ("toolbar" in viewId) score += 1
        if ("browser_toolbar" in viewId) score += 1
        if (family == BrowserFamily.CHROMIUM && "omnibox" in viewId) score += 1
        if (family == BrowserFamily.FIREFOX && "mozac" in viewId && "url" in viewId) score += 1
        if (value.source == CandidateSource.NODE_CONTENT_DESCRIPTION) score += 1

        return if (score >= 5) {
            BrowserTabSession.URL_CONFIDENCE_HIGH
        } else {
            BrowserTabSession.URL_CONFIDENCE_LOW
        }
    }

    private fun isToolbarSignal(node: BrowserNodeSnapshot): Boolean {
        val viewId = node.viewIdResourceName?.lowercase(Locale.US).orEmpty()
        return "toolbar" in viewId ||
            "omnibox" in viewId ||
            "location" in viewId ||
            "address" in viewId ||
            "browser_toolbar" in viewId ||
            "mozac" in viewId && ("title" in viewId || "url" in viewId)
    }

    private fun detectPrivacyMode(value: String, family: BrowserFamily): Int {
        val normalized = value.lowercase(Locale.US)
        return when (family) {
            BrowserFamily.CHROMIUM ->
                if (
                    "selected incognito tab" in normalized ||
                    "incognito tab" in normalized ||
                    "new incognito tab" in normalized ||
                    "leave incognito mode" in normalized ||
                    "gone incognito" in normalized ||
                    "incognito tabs" in normalized
                ) {
                    BrowserTabSession.PRIVACY_MODE_PRIVATE
                } else {
                    BrowserTabSession.PRIVACY_MODE_UNKNOWN
                }

            BrowserFamily.FIREFOX ->
                if (
                    "private browsing" in normalized ||
                    "private tab" in normalized ||
                    "new private tab" in normalized
                ) {
                    BrowserTabSession.PRIVACY_MODE_PRIVATE
                } else {
                    BrowserTabSession.PRIVACY_MODE_UNKNOWN
                }
        }
    }

    private fun detectPrivacyModeFromViewId(node: BrowserNodeSnapshot): Int {
        val viewId = node.viewIdResourceName?.lowercase(Locale.US) ?: return BrowserTabSession.PRIVACY_MODE_UNKNOWN
        return if ("incognito" in viewId) {
            BrowserTabSession.PRIVACY_MODE_PRIVATE
        } else {
            BrowserTabSession.PRIVACY_MODE_UNKNOWN
        }
    }

    private fun parseShareUrl(value: String): String? {
        val match = SHARE_LINK_PATTERN.matcher(value)
        if (!match.matches()) return null
        return match.group(1)
    }

    private fun normalizeUrl(value: String): String? {
        val trimmed = value.replace(WHITESPACE_PATTERN, " ").trim()
        return if (URL_PATTERN.matcher(trimmed).matches()) trimmed else null
    }

    private fun maxPrivacyMode(current: Int, candidate: Int): Int {
        return when {
            current == BrowserTabSession.PRIVACY_MODE_PRIVATE -> current
            candidate == BrowserTabSession.PRIVACY_MODE_PRIVATE -> candidate
            current == BrowserTabSession.PRIVACY_MODE_STANDARD -> current
            candidate == BrowserTabSession.PRIVACY_MODE_STANDARD -> candidate
            else -> BrowserTabSession.PRIVACY_MODE_UNKNOWN
        }
    }

    private data class CandidateString(
        val value: String,
        val source: CandidateSource
    )

    private enum class CandidateSource {
        NODE_TEXT,
        NODE_CONTENT_DESCRIPTION,
        NODE_PANE_TITLE
    }

    companion object {
        private val URL_PATTERN: Pattern = Pattern.compile(
            "^(https?://\\S+|www\\.\\S+|[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(?:/\\S*)?)$"
        )
        private val WHITESPACE_PATTERN = "\\s+".toRegex()
        private val SHARE_LINK_PATTERN: Pattern =
            Pattern.compile("^Share link to webpage .+?\\. Source: (.+)$")
    }
}
