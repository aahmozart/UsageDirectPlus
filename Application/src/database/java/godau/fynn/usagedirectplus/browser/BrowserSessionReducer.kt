package godau.fynn.usagedirectplus.browser

import godau.fynn.usagedirectplus.persistence.BrowserTabSession

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
            return BrowserSessionDecision.CloseAndOpen(
                openSession.id, timestamp, closeReason, observation, timestamp
            )
        }

        if (!isSameSession(openSession, observation)) {
            return BrowserSessionDecision.CloseAndOpen(
                openSession.id, timestamp, closeReason, observation, timestamp
            )
        }

        return if (isSameMetadata(openSession, observation)) {
            BrowserSessionDecision.None
        } else {
            BrowserSessionDecision.Update(openSession.id, observation)
        }
    }

    private fun isSameSession(
        openSession: BrowserTabSession,
        observation: BrowserObservation
    ): Boolean {
        if (openSession.hostname != observation.hostname) return false

        if (openSession.privacyMode != BrowserTabSession.PRIVACY_MODE_UNKNOWN &&
            observation.privacyMode != BrowserTabSession.PRIVACY_MODE_UNKNOWN &&
            openSession.privacyMode != observation.privacyMode
        ) return false

        return true
    }

    private fun isSameMetadata(
        openSession: BrowserTabSession,
        observation: BrowserObservation
    ): Boolean {
        return openSession.hostname == observation.hostname &&
            openSession.privacyMode == observation.privacyMode &&
            openSession.urlConfidence == observation.urlConfidence
    }
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
