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

        val titleCandidates = mutableListOf<TitleCandidate>()
        var privacyMode = collectEventSignals(context, family, titleCandidates)
        var strongUrl: String? = null
        var sawLowConfidenceUrl = false
        var sawToolbarSignal = false

        orderedWindows(context).forEachIndexed { windowIndex, window ->
            window.title?.let { title ->
                if (isLikelyTitle(title, family)) {
                    titleCandidates += TitleCandidate(cleanText(title), 65 - windowIndex)
                }
            }

            window.nodes.forEachIndexed { nodeIndex, node ->
                if (!node.isVisibleToUser) {
                    return@forEachIndexed
                }

                if (isToolbarSignal(node)) {
                    sawToolbarSignal = true
                }

                val values = listOfNotNull(
                    node.text?.let { CandidateString(it, CandidateSource.NODE_TEXT) },
                    node.contentDescription?.let { CandidateString(it, CandidateSource.NODE_CONTENT_DESCRIPTION) },
                    node.paneTitle?.let { CandidateString(it, CandidateSource.NODE_PANE_TITLE) }
                )

                for (value in values) {
                    privacyMode = maxPrivacyMode(privacyMode, detectPrivacyMode(value.value, family))

                    parseShareDescription(value.value)?.let { shareDescription ->
                        titleCandidates += TitleCandidate(shareDescription.title, 140 - nodeIndex - windowIndex)
                        normalizeUrl(shareDescription.url)?.let { strongUrl = it }
                    }

                    parseChromiumTabDescription(value.value)?.let { parsedTitle ->
                        titleCandidates += TitleCandidate(parsedTitle, 110 - nodeIndex - windowIndex)
                    }

                    val normalizedUrl = normalizeUrl(value.value)
                    if (normalizedUrl != null) {
                        when (scoreUrl(node, value, family)) {
                            BrowserTabSession.URL_CONFIDENCE_HIGH -> strongUrl = normalizedUrl
                            BrowserTabSession.URL_CONFIDENCE_LOW -> sawLowConfidenceUrl = true
                        }
                    }

                    val titleScore = scoreTitle(window, node, value, nodeIndex, windowIndex, family)
                    if (titleScore > 0) {
                        titleCandidates += TitleCandidate(cleanText(value.value), titleScore)
                    }
                }
            }
        }

        val title = titleCandidates
            .filter { isLikelyTitle(it.value, family) }
            .maxByOrNull(TitleCandidate::score)
            ?.value

        if (title == null && strongUrl == null) {
            return when {
                sawLowConfidenceUrl -> ExtractionResult.Rejected(ExtractionRejectReason.URL_LOW_CONFIDENCE_ONLY)
                !sawToolbarSignal -> ExtractionResult.Rejected(ExtractionRejectReason.TOOLBAR_NOT_FOUND)
                else -> ExtractionResult.Rejected(ExtractionRejectReason.NO_TITLE_CANDIDATE)
            }
        }

        return ExtractionResult.Accepted(
            BrowserObservation(
                applicationId = context.applicationId,
                title = title,
                url = strongUrl,
                privacyMode = privacyMode,
                urlConfidence = if (strongUrl != null) {
                    BrowserTabSession.URL_CONFIDENCE_HIGH
                } else {
                    BrowserTabSession.URL_CONFIDENCE_NONE
                }
            )
        )
    }

    private fun collectEventSignals(
        context: BrowserObservationContext,
        family: BrowserFamily,
        titleCandidates: MutableList<TitleCandidate>
    ): Int {
        var privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN

        context.eventContentDescription?.let { value ->
            privacyMode = maxPrivacyMode(privacyMode, detectPrivacyMode(value, family))
            parseChromiumTabDescription(value)?.let { title ->
                titleCandidates += TitleCandidate(title, 100)
            }
            if (isLikelyTitle(value, family)) {
                titleCandidates += TitleCandidate(cleanText(value), 50)
            }
        }

        context.eventTexts.forEachIndexed { index, value ->
            privacyMode = maxPrivacyMode(privacyMode, detectPrivacyMode(value, family))
            parseChromiumTabDescription(value)?.let { title ->
                titleCandidates += TitleCandidate(title, 95 - index)
            }
            if (isLikelyTitle(value, family)) {
                titleCandidates += TitleCandidate(cleanText(value), 45 - index)
            }
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

    private fun scoreTitle(
        window: BrowserWindowSnapshot,
        node: BrowserNodeSnapshot,
        value: CandidateString,
        nodeIndex: Int,
        windowIndex: Int,
        family: BrowserFamily
    ): Int {
        val cleaned = cleanText(value.value)
        if (!isLikelyTitle(cleaned, family)) {
            return 0
        }

        var score = 25 - nodeIndex.coerceAtMost(15) - windowIndex.coerceAtMost(5)

        if (window.isActive) score += 12
        if (window.isFocused) score += 8

        when (value.source) {
            CandidateSource.NODE_PANE_TITLE -> score += 45
            CandidateSource.NODE_CONTENT_DESCRIPTION -> score += 28
            CandidateSource.NODE_TEXT -> score += 18
        }

        val viewId = node.viewIdResourceName?.lowercase(Locale.US).orEmpty()
        if ("title" in viewId) score += 24
        if ("tab" in viewId) score += 12
        if ("toolbar" in viewId) score += 14
        if ("browser_toolbar" in viewId) score += 12
        if ("url" in viewId || "address" in viewId || "location" in viewId || "omnibox" in viewId) score -= 28
        if ("edit" in viewId) score -= 12
        if (node.isEditable) score -= 28

        return score
    }

    private fun scoreUrl(
        node: BrowserNodeSnapshot,
        value: CandidateString,
        family: BrowserFamily
    ): Int {
        val normalized = normalizeUrl(value.value) ?: return BrowserTabSession.URL_CONFIDENCE_NONE
        val viewId = node.viewIdResourceName?.lowercase(Locale.US).orEmpty()

        var score = 0
        if (node.isEditable) score += 3
        if ("url" in viewId || "address" in viewId || "location" in viewId || "omnibox" in viewId) score += 4
        if ("toolbar" in viewId) score += 1
        if ("browser_toolbar" in viewId) score += 1
        if (family == BrowserFamily.CHROMIUM && "omnibox" in viewId) score += 1
        if (family == BrowserFamily.FIREFOX && "mozac" in viewId && "url" in viewId) score += 1
        if (value.source == CandidateSource.NODE_CONTENT_DESCRIPTION) score += 1

        return if (score >= 5 && normalized.isNotBlank()) {
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
                    "leave incognito mode" in normalized
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

    private fun parseShareDescription(value: String): ShareDescription? {
        val match = SHARE_LINK_PATTERN.matcher(value)
        if (!match.matches()) return null

        val title = match.group(1) ?: return null
        val url = match.group(2) ?: return null
        return ShareDescription(cleanText(title), url)
    }

    private fun parseChromiumTabDescription(value: String): String? {
        val match = TAB_DESCRIPTION_PATTERN.matcher(value)
        if (!match.matches()) return null

        val title = cleanText(match.group(1) ?: return null)
        return title.takeUnless(String::isBlank)
    }

    private fun isLikelyTitle(value: String, family: BrowserFamily): Boolean {
        val cleaned = cleanText(value)
        if (cleaned.length < 3 || cleaned.length > 180) return false
        if (normalizeUrl(cleaned) != null) return false

        val normalized = cleaned.lowercase(Locale.US)
        if (normalized in GENERIC_NOISE) return false
        if (normalized == browserName(family)) return false
        if (normalized.endsWith(" tab") && cleaned.split(' ').size <= 3) return false
        return true
    }

    private fun browserName(family: BrowserFamily): String {
        return when (family) {
            BrowserFamily.CHROMIUM -> "chrome"
            BrowserFamily.FIREFOX -> "firefox"
        }
    }

    private fun cleanText(value: String): String {
        return value.replace(WHITESPACE_PATTERN, " ").trim()
    }

    private fun normalizeUrl(value: String): String? {
        val trimmed = cleanText(value)
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

    private data class TitleCandidate(
        val value: String,
        val score: Int
    )

    private data class CandidateString(
        val value: String,
        val source: CandidateSource
    )

    private data class ShareDescription(
        val title: String,
        val url: String
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
            Pattern.compile("^Share link to webpage (.+?)\\. Source: (.+)$")
        private val TAB_DESCRIPTION_PATTERN: Pattern =
            Pattern.compile("^(.+?), .*[Tt]ab$")

        private val GENERIC_NOISE = setOf(
            "chrome",
            "vanadium",
            "firefox",
            "open tabs",
            "selected tab",
            "tab",
            "incognito tab",
            "selected incognito tab",
            "private browsing",
            "private browsing session",
            "private tab",
            "new tab",
            "new private tab",
            "new incognito tab",
            "search or enter address",
            "search or type address",
            "enter search or address",
            "share"
        )
    }
}
