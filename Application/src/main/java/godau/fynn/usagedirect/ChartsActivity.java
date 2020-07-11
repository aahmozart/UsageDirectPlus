/*
 * usageDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package godau.fynn.usagedirect;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.wrapper.ComponentForegroundStat;
import godau.fynn.usagedirect.wrapper.StatsUsageInterval;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;
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

        List<Integer> accumulatedTimes = usageStatsWrapper.getAccumulatedTimes(StatsUsageInterval.DAILY, 7);

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int max = Collections.max(accumulatedTimes) + (60 * 30);

        ArrayList<String> bottomText = new ArrayList<>();
        bottomText.addAll(Arrays.asList("-7", "-6", "-5", "-4", "-3", "-2", "-1", "0"));

        BarView barView = findViewById(R.id.bar_chart);

        barView.setDataList(accumulatedTimes, max);
        barView.setBottomTextList(bottomText);

        ClockPieView pieView = findViewById(R.id.clock_chart);

        ArrayList<ClockPieHelper> clockPieHelperList = new ArrayList<>();

        List<ComponentForegroundStat> foregroundStats = usageStatsWrapper.getForegroundStatsByRelativeDay(0);

        Calendar beginCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        for (ComponentForegroundStat stat : foregroundStats) {
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
