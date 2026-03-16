package godau.fynn.usagedirectplus.wrapper;


import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.util.Log;

/**
 * “…a diminutive Guardian who traveled backward through time…”
 * <p>
 * Guards {@link EventLogWrapper} against Faulty unmatched close events (per
 * <a href="https://codeberg.org/fynngodau/usageDirect/wiki/Event-log-wrapper-scenarios">
 * docs</a>) by seeking backwards through time and scanning for the open event.
 */
public class UnmatchedCloseEventGuardian {

    private final UsageStatsManager usageStatsManager;

    private static final long SCAN_INTERVAL = 1000 * 60 * 60 * 24; // 24 hours

    UnmatchedCloseEventGuardian(UsageStatsManager usageStatsManager) {
        this.usageStatsManager = usageStatsManager;
    }

    /**
     * @param event      Event to validate
     * @param queryStart Timestamp at which original query started
     * @return True if the event is valid, false otherwise
     */
    public boolean test(UsageEvents.Event event, long queryStart) {

        UsageEvents events = usageStatsManager.queryEvents(queryStart - SCAN_INTERVAL, queryStart);

        // Iterate over events
        UsageEvents.Event e = new UsageEvents.Event();

        // Track whether the package is currently in foreground or background
        boolean open = false; // Not open until opened

        while (events.hasNextEvent()) {
            events.getNextEvent(e);

            if (e.getEventType() == UsageEvents.Event.DEVICE_STARTUP) {
                // Consider all apps closed after startup according to docs
                open = false;
            }

            // Only consider events concerning our package otherwise
            if (event.getPackageName().equals(e.getPackageName())) {
                switch (e.getEventType()) {
                    // see EventLogWrapper
                    case UsageEvents.Event.ACTIVITY_RESUMED:
                    case 4:
                        open = true;
                        break;
                    case UsageEvents.Event.ACTIVITY_PAUSED:
                    case 3:
                        if (e.getTimeStamp() == event.getTimeStamp()) {
                            // Ignore original event
                            break;
                        }
                        open = false;
                }
            }
        }

        Log.d("Guardian", "Scanned for package " + event.getPackageName() + " and determined event to be " +
                (open ? "True" : "Faulty")
        );

        // Event is valid if it was previously opened (within SCAN_INTERVAL)
        return open;
    }
}
