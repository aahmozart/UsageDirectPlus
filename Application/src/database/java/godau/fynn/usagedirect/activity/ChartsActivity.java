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

package godau.fynn.usagedirect.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.UsageStatsDao;
import godau.fynn.usagedirect.view.UsageStatBarView;
import godau.fynn.usagedirect.view.WeeklyAverageBarView;
import godau.fynn.usagedirect.view.adapter.ClockPieViewPagerAdapter;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

import java.util.Map;

import static godau.fynn.usagedirect.persistence.HistoryDatabase.DATABASE_NAME;

public class ChartsActivity extends Activity {

    private Map<Day, Long> usagePerDayMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_charts);

        // Display data from database in bar view
        final UsageStatBarView barView = findViewById(R.id.bar_view);
        final WeeklyAverageBarView averageBarView = findViewById(R.id.average_bar_view);
        new Thread(new Runnable() {
            @Override
            public void run() {

                HistoryDatabase database = Room.databaseBuilder(ChartsActivity.this, HistoryDatabase.class, DATABASE_NAME).build();
                UsageStatsDao usageStats = database.getUsageStatsDao();

                usagePerDayMap = usageStats.getTotalTimePerDay();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        barView.setData(usagePerDayMap);
                        barView.getBarView().setBoldPosition(usagePerDayMap.keySet().size() - 1);
                        barView.scrollToEnd();

                        averageBarView.setData(usagePerDayMap);
                    }
                });
            }
        }).start();


        final ViewPager clockPager = findViewById(R.id.clock_pie_view_pager);

        clockPager.setAdapter(new ClockPieViewPagerAdapter(this, new UsageStatsWrapper(ChartsActivity.this)));
        clockPager.setCurrentItem(9);

        SmartTabLayout chartTabLayout = findViewById(R.id.clock_pie_view_pager_tab);
        chartTabLayout.setViewPager(clockPager);

        clockPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (usagePerDayMap != null) {
                    barView.getBarView().setBoldPosition(usagePerDayMap.keySet().size() - clockPager.getAdapter().getCount() + position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }
}
