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
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.usagedirect.view.BarViewPagerAdapter;
import godau.fynn.usagedirect.view.ClockPieViewPagerAdapter;
import godau.fynn.usagedirect.view.FramedBarView;
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

        ViewPager barPager = findViewById(R.id.bar_view_pager);

        barPager.setAdapter(new BarViewPagerAdapter(this, usageStatsWrapper));

        SmartTabLayout barTabLayout = findViewById(R.id.bar_view_pager_tab);
        barTabLayout.setViewPager(barPager);

        ViewPager clockPager = findViewById(R.id.clock_pie_view_pager);

        clockPager.setAdapter(new ClockPieViewPagerAdapter(this, usageStatsWrapper));
        clockPager.setCurrentItem(9);

        SmartTabLayout chartTabLayout = findViewById(R.id.clock_pie_view_pager_tab);
        chartTabLayout.setViewPager(clockPager);

    }
}
