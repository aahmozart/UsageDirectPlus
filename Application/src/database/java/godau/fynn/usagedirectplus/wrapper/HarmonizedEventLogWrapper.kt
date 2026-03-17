package godau.fynn.usagedirectplus.wrapper

import android.content.Context

/**
 * Used for displaying clock pie charts, not for precise usage time calculations
 *
 * @see EventLogWrapper
 */
class HarmonizedEventLogWrapper(context: Context) : EventLogWrapper(context) {

    /**
     * @return A harmonized version of the result of parent's method
     */
    override fun getForegroundStatsByTimestamps(start: Long, end: Long): List<ComponentForegroundStat> {
        val eventList = super.getForegroundStatsByTimestamps(start, end)

        if (eventList.size <= 1) return eventList

        // Harmonize

        val harmonizedList = ArrayList<ComponentForegroundStat>()

        var pendingEvent = eventList[0]
        for (i in 1 until eventList.size) {
            var currentEvent = eventList[i]

            // Merge equal package name events if less than `HARMONY_INTERVAL` between usages
            if (currentEvent.packageName == pendingEvent.packageName &&
                Math.abs(currentEvent.beginTime - pendingEvent.endTime) < HARMONY_INTERVAL
            ) {
                // Merge current with pending event
                pendingEvent = ComponentForegroundStat(
                    pendingEvent.beginTime, currentEvent.endTime, currentEvent.packageName
                )
            } else {
                // Harmonize start time if less than `HARMONY_INTERVAL` between usages
                if (Math.abs(currentEvent.beginTime - pendingEvent.endTime) < HARMONY_INTERVAL) {
                    // Harmonize end and start times
                    currentEvent = ComponentForegroundStat(
                        pendingEvent.endTime, currentEvent.endTime, currentEvent.packageName
                    )
                }

                // Commit pending event
                harmonizedList.add(pendingEvent)
                // Current event is new pending event
                pendingEvent = currentEvent
            }
        }

        // Commit last pending event
        harmonizedList.add(pendingEvent)

        return harmonizedList
    }

    companion object {
        /**
         * Events that are closer than 30s should be merged
         */
        private const val HARMONY_INTERVAL = 30 * 1000
    }
}
