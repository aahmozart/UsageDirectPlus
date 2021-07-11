package godau.fynn.usagedirect.thread.icon;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.util.List;

public class AppUsageStatisticsIconThread extends IconThread {
    public AppUsageStatisticsIconThread(List<SimpleUsageStat> usageStats, RecyclerView.LayoutManager layout, Activity context) {
        super(usageStats.stream().map((u) -> u.getApplicationId()).toArray(String[]::new), layout, context);
    }

    @Override
    protected void onIconLoaded(int position, String applicationId) {
        super.onIconLoaded(position + 1, applicationId);
    }
}
