package godau.fynn.usagedirectplus.thread.icon

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import godau.fynn.usagedirectplus.SimpleUsageStat

class AppUsageStatisticsIconThread(
    usageStats: List<SimpleUsageStat>,
    layout: RecyclerView.LayoutManager,
    context: Context
) : IconThread(
    usageStats.map { it.applicationId }.toTypedArray(),
    layout,
    context
) {
    override fun onIconLoaded(position: Int, applicationId: String) {
        super.onIconLoaded(position + 1, applicationId)
    }
}
