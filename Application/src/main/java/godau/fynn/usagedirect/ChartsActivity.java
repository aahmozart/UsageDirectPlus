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
import android.widget.HorizontalScrollView;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.usagedirect.view.ClockPieViewPagerAdapter;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;
import im.dacer.androidcharts.BarView;

import java.util.*;

public class ChartsActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_charts);

        getActionBar().setTitle(R.string.title_feature_preview);

        UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(this);

        List<Integer> accumulatedTimes = usageStatsWrapper.getAccumulatedTimes(Interval.DAILY, 9);

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int max = Collections.max(accumulatedTimes) + (60 * 30);

        ArrayList<String> bottomText = new ArrayList<>();

        for (int i = 9; i >= 0; i--) {
            bottomText.add(NaturalText.formatShort(Interval.DAILY, i));
        }

        BarView barView = findViewById(R.id.bar_chart);

        barView.setDataList(accumulatedTimes, max);
        barView.setBottomTextList(bottomText);

        final HorizontalScrollView barScrollView = findViewById(R.id.bar_chart_scroll);

        barScrollView.post(new Runnable() {
            @Override
            public void run() {
                barScrollView.scrollTo(5000, 0);
            }
        });

        ViewPager clockPager = findViewById(R.id.clock_pie_view_pager);

        clockPager.setAdapter(new ClockPieViewPagerAdapter(this, usageStatsWrapper));
        clockPager.setCurrentItem(9);

        SmartTabLayout tabLayout = findViewById(R.id.clock_pie_view_pager_tab);
        tabLayout.setViewPager(clockPager);

    }
}
