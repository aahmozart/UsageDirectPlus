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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.EventLogRunnable;
import godau.fynn.usagedirect.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirect.view.adapter.database.DatabaseTimespanPagerAdapter;

public class DatabaseAppUsageStatisticsActivity extends AppUsageStatisticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_app_usage_statistics);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void prepare() {
        new EventLogRunnable(this).run();
    }

    @Override
    protected TimespanPagerAdapter getAdapter() {
        return new DatabaseTimespanPagerAdapter(this);
    }

    @Override
    protected void onReload(final ViewPager viewPager, final ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                new EventLogRunnable(DatabaseAppUsageStatisticsActivity.this).run();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
}
