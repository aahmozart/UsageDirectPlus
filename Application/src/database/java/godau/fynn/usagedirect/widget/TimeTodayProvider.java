package godau.fynn.usagedirect.widget;

import android.content.Context;
import godau.fynn.usagedirect.wrapper.EventLogWrapper;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

public class TimeTodayProvider {

    public static long getTimeToday(Context context) {
        EventLogWrapper eventLogWrapper = new EventLogWrapper(context);

        return EventLogWrapper.aggregateSimpleUsageStats(
                eventLogWrapper.aggregateForegroundStats(
                        eventLogWrapper.getForegroundStatsByRelativeDay(0)
                )
        );
    }
}
