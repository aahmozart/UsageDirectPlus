package godau.fynn.usagedirect.widget;

import android.content.Context;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

public class TimeTodayProvider {

    public static long getTimeToday(Context context) {
        UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(context);

        return UsageStatsWrapper.aggregateSimpleUsageStats(
                usageStatsWrapper.aggregateForegroundStats(
                        usageStatsWrapper.getForegroundStatsByRelativeDay(0)
                )
        );
    }
}
