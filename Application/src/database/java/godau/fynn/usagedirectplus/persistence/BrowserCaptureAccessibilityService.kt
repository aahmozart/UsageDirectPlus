package godau.fynn.usagedirectplus.persistence

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import godau.fynn.usagedirectplus.browser.BrowserObservation
import godau.fynn.usagedirectplus.browser.BrowserObservationExtractor
import godau.fynn.usagedirectplus.browser.BrowserWindowSnapshotFactory
import godau.fynn.usagedirectplus.browser.ExtractionResult
import godau.fynn.usagedirectplus.browser.BrowserSessionDecision
import godau.fynn.usagedirectplus.browser.BrowserSessionReducer
import godau.fynn.usagedirectplus.browser.BrowserSupport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserCaptureAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val extractor = BrowserObservationExtractor()

    @Volatile
    private var browserSessionOpen = false

    @Volatile
    private var lastObservationSignature: String? = null

    @Volatile
    private var lastObservationAt: Long = 0

    override fun onServiceConnected() {
        Log.i(TAG, "Accessibility service connected")
        serviceScope.launch {
            val database = HistoryDatabase.get(this@BrowserCaptureAccessibilityService)
            try {
                browserSessionOpen = database.getBrowserTabSessionDao().getOpenSession() != null
                Log.d(TAG, "Open browser session at connect: $browserSessionOpen")
            } finally {
                database.close()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventPackage = event.packageName?.toString()
        val timestamp = BrowserCaptureTimestampResolver.resolve(event.eventTime)
        val activeRoot = rootInActiveWindow
        val rootPackage = activeRoot?.packageName?.toString()
        val activePackage = rootPackage ?: eventPackage ?: run {
            activeRoot?.recycle()
            return
        }
        val isSupportedBrowser = BrowserSupport.isSupportedBrowser(activePackage)

        if (!isSupportedBrowser && !browserSessionOpen) {
            activeRoot?.recycle()
            return
        }

        val extractionResult = if (isSupportedBrowser) {
            val interactiveWindows = windows ?: emptyList()
            Log.d(
                TAG,
                "Browser event package=$activePackage type=${event.eventType} windows=${interactiveWindows.size}"
            )
            val context = BrowserWindowSnapshotFactory.create(
                applicationId = activePackage,
                event = event,
                activeRoot = if (interactiveWindows.isEmpty()) activeRoot else null,
                windows = interactiveWindows
            )
            if (interactiveWindows.isNotEmpty()) {
                activeRoot?.recycle()
            }
            extractor.extract(context)
        } else {
            activeRoot?.recycle()
            null
        }

        val observation = (extractionResult as? ExtractionResult.Accepted)?.observation
        if (observation != null && shouldThrottle(observation, timestamp)) {
            BrowserCaptureDiagnostics.recordAccepted(this, activePackage, timestamp)
            return
        }

        serviceScope.launch {
            val database = HistoryDatabase.get(this@BrowserCaptureAccessibilityService)
            try {
                val dao = database.getBrowserTabSessionDao()
                val openSession = dao.getOpenSession()
                val decision = when {
                    observation != null -> BrowserSessionReducer.reduce(
                        openSession,
                        observation,
                        timestamp,
                        BrowserTabSession.CLOSE_REASON_SWITCHED
                    )

                    isSupportedBrowser -> BrowserSessionDecision.None
                    else -> BrowserSessionReducer.reduce(
                        openSession,
                        null,
                        timestamp,
                        closeReasonFor(event)
                    )
                }

                when (val result = extractionResult) {
                    is ExtractionResult.Accepted -> {
                        Log.i(
                            TAG,
                            "Accepted browser observation package=$activePackage hostname=${result.observation.hostname}"
                        )
                        BrowserCaptureDiagnostics.recordAccepted(
                            this@BrowserCaptureAccessibilityService,
                            activePackage,
                            timestamp
                        )
                    }

                    is ExtractionResult.Rejected -> {
                        Log.w(
                            TAG,
                            "Rejected browser observation package=$activePackage reason=${result.reason}"
                        )
                        BrowserCaptureDiagnostics.recordRejected(
                            this@BrowserCaptureAccessibilityService,
                            activePackage,
                            timestamp,
                            result.reason
                        )
                    }

                    null -> Unit
                }

                applyDecision(dao, openSession, decision)
            } finally {
                database.close()
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
        closeOpenSession(BrowserTabSession.CLOSE_REASON_SERVICE_STOPPED)
    }

    override fun onDestroy() {
        Log.w(TAG, "Accessibility service destroying")
        closeOpenSessionNow(BrowserTabSession.CLOSE_REASON_SERVICE_STOPPED)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun applyDecision(
        dao: BrowserTabSessionDao,
        openSession: BrowserTabSession?,
        decision: BrowserSessionDecision
    ) {
        when (decision) {
            BrowserSessionDecision.None -> Unit

            is BrowserSessionDecision.Open -> {
                Log.d(TAG, "Opening browser session for ${decision.observation.applicationId}")
                dao.insertOpenSession(
                    applicationId = decision.observation.applicationId,
                    openedAt = decision.openedAt,
                    hostname = decision.observation.hostname,
                    privacyMode = decision.observation.privacyMode,
                    urlConfidence = decision.observation.urlConfidence
                )
                browserSessionOpen = true
            }

            is BrowserSessionDecision.Update -> {
                Log.d(TAG, "Updating browser session id=${decision.sessionId}")
                dao.updateOpenSessionMetadata(
                    id = decision.sessionId,
                    hostname = decision.observation.hostname,
                    privacyMode = decision.observation.privacyMode,
                    urlConfidence = decision.observation.urlConfidence
                )
                browserSessionOpen = true
            }

            is BrowserSessionDecision.Close -> {
                val current = openSession ?: return
                Log.d(TAG, "Closing browser session id=${decision.sessionId} reason=${decision.closeReason}")
                dao.closeOpenSession(
                    id = decision.sessionId,
                    closedAt = decision.closedAt,
                    closeReason = decision.closeReason,
                    hostname = current.hostname,
                    privacyMode = current.privacyMode,
                    urlConfidence = current.urlConfidence
                )
                browserSessionOpen = false
                clearThrottleState()
            }

            is BrowserSessionDecision.CloseAndOpen -> {
                val current = openSession ?: return
                Log.d(TAG, "Switching browser session id=${decision.sessionId} reason=${decision.closeReason}")
                dao.closeOpenSession(
                    id = decision.sessionId,
                    closedAt = decision.closedAt,
                    closeReason = decision.closeReason,
                    hostname = current.hostname,
                    privacyMode = current.privacyMode,
                    urlConfidence = current.urlConfidence
                )
                dao.insertOpenSession(
                    applicationId = decision.observation.applicationId,
                    openedAt = decision.openedAt,
                    hostname = decision.observation.hostname,
                    privacyMode = decision.observation.privacyMode,
                    urlConfidence = decision.observation.urlConfidence
                )
                browserSessionOpen = true
            }
        }
    }

    private fun closeOpenSession(closeReason: Int) {
        serviceScope.launch {
            val database = HistoryDatabase.get(this@BrowserCaptureAccessibilityService)
            try {
                val dao = database.getBrowserTabSessionDao()
                val openSession = dao.getOpenSession() ?: return@launch
                dao.closeOpenSession(
                    id = openSession.id,
                    closedAt = System.currentTimeMillis(),
                    closeReason = closeReason,
                    hostname = openSession.hostname,
                    privacyMode = openSession.privacyMode,
                    urlConfidence = openSession.urlConfidence
                )
                browserSessionOpen = false
                clearThrottleState()
                Log.d(TAG, "Closed open session asynchronously reason=$closeReason")
            } finally {
                database.close()
            }
        }
    }

    private fun closeOpenSessionNow(closeReason: Int) {
        runBlocking {
            withContext(Dispatchers.IO) {
                val database = HistoryDatabase.get(this@BrowserCaptureAccessibilityService)
                try {
                    val dao = database.getBrowserTabSessionDao()
                    val openSession = dao.getOpenSession() ?: return@withContext
                    dao.closeOpenSession(
                        id = openSession.id,
                        closedAt = System.currentTimeMillis(),
                        closeReason = closeReason,
                        hostname = openSession.hostname,
                        privacyMode = openSession.privacyMode,
                        urlConfidence = openSession.urlConfidence
                    )
                    browserSessionOpen = false
                    clearThrottleState()
                    Log.d(TAG, "Closed open session synchronously reason=$closeReason")
                } finally {
                    database.close()
                }
            }
        }
    }

    private fun shouldThrottle(observation: BrowserObservation, timestamp: Long): Boolean {
        val signature = buildString {
            append(observation.applicationId)
            append('|')
            append(observation.hostname)
            append('|')
            append(observation.privacyMode)
            append('|')
            append(observation.urlConfidence)
        }

        val isDuplicate = signature == lastObservationSignature && timestamp - lastObservationAt < THROTTLE_WINDOW_MS
        lastObservationSignature = signature
        lastObservationAt = timestamp
        return isDuplicate
    }

    private fun clearThrottleState() {
        lastObservationSignature = null
        lastObservationAt = 0
    }

    private fun closeReasonFor(event: AccessibilityEvent): Int {
        return if (event.packageName == null) {
            BrowserTabSession.CLOSE_REASON_WINDOW_LOST
        } else {
            BrowserTabSession.CLOSE_REASON_APP_BACKGROUND
        }
    }

    companion object {
        private const val TAG = "BrowserCapture"
        private const val THROTTLE_WINDOW_MS = 750L

        fun isEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val expected = ComponentName(context, BrowserCaptureAccessibilityService::class.java)
                .flattenToString()

            return enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
        }
    }
}
