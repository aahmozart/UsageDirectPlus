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
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirect.BuildConfig;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.EventLogRunnable;
import godau.fynn.usagedirect.view.adapter.database.DatabaseTimespanPagerAdapter;
import godau.fynn.usagedirect.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirect.view.dialog.GrantPermissionDialog;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.librariesdirect.AboutLibrariesActivity;
import godau.fynn.librariesdirect.AboutLibrariesConfig;
import godau.fynn.librariesdirect.Library;
import godau.fynn.librariesdirect.License;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

/**
 * Launcher Activity for the App Usage Statistics sample app.
 */
public class AppUsageStatisticsActivity extends Activity {

    private ViewPager viewPager;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_statistics);

        final SmartTabLayout tabs = findViewById(R.id.viewpagertab);

        tabs.setElevation(getActionBar().getElevation());

        getActionBar().setElevation(0f);


        if (!new UsageStatsWrapper(this).isPermissionGranted()) {
            new GrantPermissionDialog(this).show();
        }

        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {

                new EventLogRunnable(AppUsageStatisticsActivity.this).run();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final TimespanPagerAdapter timespanAdapter = new DatabaseTimespanPagerAdapter(AppUsageStatisticsActivity.this);

                        viewPager = findViewById(R.id.timespanpager);
                        viewPager.setOffscreenPageLimit(3);
                        viewPager.setAdapter(timespanAdapter);

                        viewPager.addOnPageChangeListener(timespanAdapter);

                        viewPager.setPageMargin(
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())
                        );
                        viewPager.setPageMarginDrawable(new ColorDrawable(getResources().getColor(R.color.page_switch_indicator)));

                        tabs.setViewPager(viewPager);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:

                AboutLibrariesConfig.setLibraries(new Library[]{
                        new Library("usageDirect" + ' ' + BuildConfig.VERSION_NAME, License.GNU_GPL_V3_OR_LATER_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/usageDirect"),
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

            case R.id.menu_system_stats:
                startActivity(new Intent(this, SystemStatsActivity.class));
                break;

            case R.id.menu_database:
                startActivity(new Intent(this, DatabaseManagerActivity.class));
                break;

            case R.id.menu_reload:
                progressBar.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new EventLogRunnable(AppUsageStatisticsActivity.this).run();
                        ((DatabaseTimespanPagerAdapter) viewPager.getAdapter()).prepare(0);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                viewPager.getAdapter().notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();
                break;
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

    @Override
    protected void onStop() {
        UsageStatsWrapper.flushCache();
        super.onStop();
    }
}
