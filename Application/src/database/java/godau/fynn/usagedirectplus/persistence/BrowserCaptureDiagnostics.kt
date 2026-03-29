package godau.fynn.usagedirectplus.persistence

import android.content.Context
import godau.fynn.usagedirectplus.browser.ExtractionRejectReason
import godau.fynn.usagedirectplus.R

data class BrowserCaptureStatus(
    val packageName: String?,
    val timestamp: Long,
    val result: String?,
    val detail: String?
)

object BrowserCaptureDiagnostics {
    private const val KEY_PACKAGE = "browserCaptureLastPackage"
    private const val KEY_TIMESTAMP = "browserCaptureLastTimestamp"
    private const val KEY_RESULT = "browserCaptureLastResult"
    private const val KEY_DETAIL = "browserCaptureLastDetail"

    fun recordAccepted(context: Context, packageName: String, timestamp: Long) {
        write(
            context = context,
            packageName = packageName,
            timestamp = timestamp,
            result = RESULT_ACCEPTED,
            detail = DETAIL_ACCEPTED_WITH_HOSTNAME
        )
    }

    fun recordRejected(
        context: Context,
        packageName: String,
        timestamp: Long,
        reason: ExtractionRejectReason
    ) {
        write(
            context = context,
            packageName = packageName,
            timestamp = timestamp,
            result = RESULT_REJECTED,
            detail = reason.name
        )
    }

    fun load(context: Context): BrowserCaptureStatus {
        val prefs = context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
        return BrowserCaptureStatus(
            packageName = prefs.getString(KEY_PACKAGE, null),
            timestamp = prefs.getLong(KEY_TIMESTAMP, 0),
            result = prefs.getString(KEY_RESULT, null),
            detail = prefs.getString(KEY_DETAIL, null)
        )
    }

    fun formatResult(context: Context, status: BrowserCaptureStatus): String {
        return when (status.result) {
            RESULT_ACCEPTED -> {
                val detail = context.getString(R.string.browser_capture_status_hostname)
                context.getString(R.string.browser_capture_status_accepted, detail)
            }

            RESULT_REJECTED -> context.getString(
                R.string.browser_capture_status_rejected,
                formatReason(context, status.detail)
            )

            else -> context.getString(R.string.browser_capture_status_no_attempts)
        }
    }

    fun formatReason(context: Context, detail: String?): String {
        return when (detail) {
            ExtractionRejectReason.NO_SUPPORTED_WINDOWS.name ->
                context.getString(R.string.browser_capture_reason_no_supported_windows)

            ExtractionRejectReason.NO_VISIBLE_NODES.name ->
                context.getString(R.string.browser_capture_reason_no_visible_nodes)

            ExtractionRejectReason.TOOLBAR_NOT_FOUND.name ->
                context.getString(R.string.browser_capture_reason_toolbar_not_found)

            ExtractionRejectReason.NO_HOSTNAME.name ->
                context.getString(R.string.browser_capture_reason_no_hostname)

            ExtractionRejectReason.URL_LOW_CONFIDENCE_ONLY.name ->
                context.getString(R.string.browser_capture_reason_url_low_confidence_only)

            else -> context.getString(R.string.browser_capture_reason_unknown)
        }
    }

    private fun write(
        context: Context,
        packageName: String,
        timestamp: Long,
        result: String,
        detail: String
    ) {
        context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PACKAGE, packageName)
            .putLong(KEY_TIMESTAMP, timestamp)
            .putString(KEY_RESULT, result)
            .putString(KEY_DETAIL, detail)
            .apply()
    }

    const val RESULT_ACCEPTED = "accepted"
    const val RESULT_REJECTED = "rejected"
    const val DETAIL_ACCEPTED_WITH_HOSTNAME = "HOSTNAME"
}
