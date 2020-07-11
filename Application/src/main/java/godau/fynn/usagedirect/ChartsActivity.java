package godau.fynn.usagedirect;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import godau.fynn.usagedirect.R;
import im.dacer.androidcharts.BarView;
import im.dacer.androidcharts.ClockPieHelper;
import im.dacer.androidcharts.ClockPieView;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChartsActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_charts);

        getActionBar().setTitle(R.string.title_feature_preview);

        UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(this);

        List<Integer> accumulatedTimes = usageStatsWrapper.getAccumulatedTimes(UsageStatsWrapper.StatsUsageInterval.DAILY, 7);

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int max = Collections.max(accumulatedTimes) + (60 * 30);

        ArrayList<String> bottomText = new ArrayList<>();
        bottomText.addAll(Arrays.asList("-7", "-6", "-5", "-4", "-3", "-2", "-1", "0"));

        BarView barView = findViewById(R.id.bar_chart);

        barView.setDataList(accumulatedTimes, max);
        barView.setBottomTextList(bottomText);

        ClockPieView pieView = findViewById(R.id.clock_chart);

        ArrayList<ClockPieHelper> clockPieHelperList = new ArrayList<>();

        List<UsageStatsWrapper.ComponentForegroundStat> foregroundStats = usageStatsWrapper.getForegroundStatsByRelativeDay(0);

        Calendar beginCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        for (UsageStatsWrapper.ComponentForegroundStat stat : foregroundStats) {
            beginCalendar.setTimeInMillis(stat.beginTime);
            endCalendar.setTimeInMillis(stat.endTime);
            clockPieHelperList.add(new ClockPieHelper(
                    beginCalendar.get(Calendar.HOUR_OF_DAY), beginCalendar.get(Calendar.MINUTE), beginCalendar.get(Calendar.SECOND),
                    endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), endCalendar.get(Calendar.SECOND)
            ));
            Log.d("ChartsActivity", "Stat begins at " + sdf.format(beginCalendar.getTime()) + ", ends at "
                    + sdf.format(endCalendar.getTime()) + " and is of package " + stat.packageName);
            Log.d("ChartsActivity rep", Arrays.asList(beginCalendar.get(Calendar.HOUR_OF_DAY), beginCalendar.get(Calendar.MINUTE), beginCalendar.get(Calendar.SECOND),
                    endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), endCalendar.get(Calendar.SECOND)).toString());

        }

        Log.d("ChartsActivity", "Displaying " + foregroundStats.size() + " foreground stats");

        pieView.setDate(clockPieHelperList);


    }
}
