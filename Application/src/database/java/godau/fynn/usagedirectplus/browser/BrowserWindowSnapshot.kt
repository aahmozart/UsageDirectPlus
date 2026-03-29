package godau.fynn.usagedirectplus.browser

data class BrowserObservationContext(
    val applicationId: String,
    val eventTexts: List<String> = emptyList(),
    val eventContentDescription: String? = null,
    val eventClassName: String? = null,
    val activeWindowId: Int? = null,
    val windows: List<BrowserWindowSnapshot> = emptyList()
) {
    fun hasVisibleNodes(): Boolean = windows.any { window ->
        window.nodes.any(BrowserNodeSnapshot::isVisibleToUser)
    }
}

data class BrowserWindowSnapshot(
    val id: Int? = null,
    val title: String? = null,
    val packageName: String? = null,
    val isActive: Boolean = false,
    val isFocused: Boolean = false,
    val nodes: List<BrowserNodeSnapshot> = emptyList()
)

data class BrowserNodeSnapshot(
    val viewIdResourceName: String? = null,
    val text: String? = null,
    val contentDescription: String? = null,
    val paneTitle: String? = null,
    val className: String? = null,
    val isEditable: Boolean = false,
    val isVisibleToUser: Boolean = true
)

sealed interface ExtractionResult {
    data class Accepted(val observation: BrowserObservation) : ExtractionResult

    data class Rejected(val reason: ExtractionRejectReason) : ExtractionResult
}

enum class ExtractionRejectReason {
    NO_SUPPORTED_WINDOWS,
    NO_VISIBLE_NODES,
    TOOLBAR_NOT_FOUND,
    NO_HOSTNAME,
    URL_LOW_CONFIDENCE_ONLY
}
