/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package godau.fynn.usagedirect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.view.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.view.dialog.GrantPermissionDialog;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.librariesdirect.AboutLibrariesActivity;
import godau.fynn.librariesdirect.AboutLibrariesConfig;
import godau.fynn.librariesdirect.Library;
import godau.fynn.librariesdirect.License;

import java.util.*;

/**
 * Launcher Activity for the App Usage Statistics sample app.
 */
public class AppUsageStatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_statistics);

        SmartTabLayout tabs = findViewById(R.id.viewpagertab);

        tabs.setElevation(getActionBar().getElevation());

        getActionBar().setElevation(0f);


        final UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(AppUsageStatisticsActivity.this);

        if (!usageStatsWrapper.isPermissionGranted()) {
            new GrantPermissionDialog(this).show();
        }

        final PagerAdapter timespanAdapter = new PagerAdapter() {

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {

                View view = getLayoutInflater().inflate(R.layout.content_timespan, container, false);
                container.addView(view);

                SmartTabLayout tabLayout = view.findViewById(R.id.viewpagertab);

                ViewPager viewPager = view.findViewById(R.id.viewpager);
                viewPager.setAdapter(
                        new UsageListViewPagerAdapter(UsageStatsWrapper.StatsUsageInterval.values()[position],
                                AppUsageStatisticsActivity.this)
                );

                viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                viewPager.setOffscreenPageLimit(3);

                tabLayout.setViewPager(viewPager);

                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object || ((View) object).getParent() == object;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0: return getString(R.string.span_daily);
                    case 1: return getString(R.string.span_weekly);
                    case 2: return getString(R.string.span_monthly);
                    case 3: return getString(R.string.span_yearly);
                    default: return null;
                }
            }
        };

        ViewPager viewPager = findViewById(R.id.timespanpager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(timespanAdapter);

        viewPager.setPageMargin(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())
        );
        viewPager.setPageMarginDrawable(new ColorDrawable(getColor(R.color.page_switch_indicator)));

        tabs.setViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:

                AboutLibrariesConfig.setLibraries(new Library[]{
                        new Library("usageDirect", License.GNU_GPL_V3_OR_LATER_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/usageDirect"),
                        new Library("AppUsageStatistics", License.APACHE_20_LICENSE, null, "The Android Open Source Project, Inc", "https://github.com/googlesamples/android-AppUsageStatistics"),
                        new Library("AndroidCharts", License.MIT_LICENSE, "The MIT License (MIT)\n" +
                                "\n" +
                                "Copyright (c) 2013 Ding Wenhao", "Ding Wenhao", "https://github.com/HackPlan/AndroidCharts"),
                        new Library("Humanize", License.APACHE_20_LICENSE, null, "mfornos", "http://mfornos.github.io/humanize/"),
                        new Library("SmartTabLayout", License.APACHE_20_LICENSE, null, "ogaclejapan", "https://github.com/ogaclejapan/SmartTabLayout"),
                        new Library("librariesDirect", License.CC0_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/librariesDirect"),
                });

                AboutLibrariesConfig.setHeaderText(getString(R.string.about_libraries_header));

                startActivity(new Intent(this, AboutLibrariesActivity.class));
                break;

            case R.id.menu_charts:
                startActivity(new Intent(this, ChartsActivity.class));
                break;

            case R.id.menu_test:

                UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(this);

                int eventDays = 0;
                List<UsageStatsWrapper.ComponentForegroundStat> events;
                do {
                    events = usageStatsWrapper.getForegroundStatsByRelativeDay(eventDays++);
                } while (events.size() > 0);

                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_test)
                        .setMessage(getString(R.string.test_result,
                                usageStatsWrapper.getDatasetAmount(UsageStatsWrapper.StatsUsageInterval.DAILY),
                                --eventDays,
                                usageStatsWrapper.getDatasetAmount(UsageStatsWrapper.StatsUsageInterval.WEEKLY),
                                usageStatsWrapper.getDatasetAmount(UsageStatsWrapper.StatsUsageInterval.MONTHLY),
                                usageStatsWrapper.getDatasetAmount(UsageStatsWrapper.StatsUsageInterval.YEARLY)
                        ))
                        .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GrantPermissionDialog.REQUEST_CODE)
            recreate();
    }
}
