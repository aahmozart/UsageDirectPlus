package godau.fynn.usagedirect.view;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import godau.fynn.usagedirect.Comparator;
import godau.fynn.usagedirect.IconThread;
import godau.fynn.usagedirect.UsageStatsWrapper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class UsageListViewPagerAdapter extends PagerAdapter {
    private final Queue<UsageListView> recycleViewList = new LinkedList<>();
    private final UsageStatsWrapper.StatsUsageInterval interval;
    private final Activity context;

    private int count = -1;

    private static UsageStatsWrapper usageStats;

    public UsageListViewPagerAdapter(UsageStatsWrapper.StatsUsageInterval interval, Activity context) {
        this.interval = interval;
        this.context = context;

        if (usageStats == null) {
            usageStats = new UsageStatsWrapper(context);
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        // Setup view

        final UsageListView usageListView;
        if (recycleViewList.peek() == null) {
            usageListView = new UsageListView(context);
        } else {
            Log.d("ULVPA", "Recycling usage list view from recycle bin");
            usageListView = recycleViewList.poll();
            usageListView.setUsageStatsList(null);
        }
        container.addView(usageListView);


        // Get data

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<UsageStats> usageStatsList = usageStats.getUsageStatistics(interval, getCount() - position - 1);

                // Filter unused apps
                for (int i = usageStatsList.size() - 1; i >= 0; i--) {
                    UsageStats usageStats = usageStatsList.get(i);
                    if (usageStats.getTotalTimeInForeground() <= 0)
                        usageStatsList.remove(i);
                }

                Collections.sort(usageStatsList, new Comparator.TimeInForegroundComparatorDesc());

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usageListView.setUsageStatsList(usageStatsList);

                        // Get missing icons from system
                        new IconThread(usageStatsList, usageListView.getLayoutManager(), context).start();
                    }
                });



            }
        }).start();

        return usageListView;
    }

    @Override
    public int getCount() {
        if (count == -1) count = usageStats.getDatasetAmount(interval);
        return count;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Log.d("ULVPA", "Moving item " + position + " to recycle bin");
        container.removeView((View) object);
        recycleViewList.add((UsageListView) object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "Span -" + (getCount() - position - 1);
    }
}
