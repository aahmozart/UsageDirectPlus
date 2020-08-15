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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirect.BuildConfig;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.EventLogRunnable;
import godau.fynn.usagedirect.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.view.adapter.database.DatabaseTimespanPagerAdapter;
import godau.fynn.usagedirect.view.adapter.database.DatabaseUsageListViewPagerAdapter;
import godau.fynn.usagedirect.view.dialog.DatabaseDebugDialog;

/**
 * Different implementation of AUSA for the two source flavors
 */
public class SourceAppUsageStatisticsActivity extends AppUsageStatisticsActivity {

    private DatabaseTimespanPagerAdapter databaseTimespanPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_app_usage_statistics);
        super.onCreate(savedInstanceState);

        databaseTimespanPagerAdapter = new DatabaseTimespanPagerAdapter(SourceAppUsageStatisticsActivity.this);
    }

    @Override
    protected void prepare() {
        new EventLogRunnable(this).run();

        databaseTimespanPagerAdapter.prepare(0);
    }

    @Override
    protected void setAdapter(ViewPager viewPager) {
        UsageListViewPagerAdapter usageListViewPagerAdapter = databaseTimespanPagerAdapter.getUsageListViewPagerAdapter(0);
        viewPager.setAdapter(usageListViewPagerAdapter);
        viewPager.setCurrentItem(usageListViewPagerAdapter.getCount());
    }

    @Override
    protected void onReload(final ViewPager viewPager, final ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepare();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        databaseTimespanPagerAdapter.notifyDataSetChanged();
                        viewPager.getAdapter().notifyDataSetChanged();

                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals(getString(R.string.menu_database))) {
            new DatabaseDebugDialog(this).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            menu.add(R.string.menu_database);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }
}
