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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.view.adapter.system.SystemTimespanPagerAdapter;
import godau.fynn.usagedirect.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirect.view.dialog.GrantPermissionDialog;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

/**
 * Different implementation of AUSA for the two source flavors
 */
public class SourceAppUsageStatisticsActivity extends AppUsageStatisticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_system_stats);
        super.onCreate(savedInstanceState);

        getActionBar().setElevation(0f);
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

    @Override
    protected void prepare() {
    }

    @Override
    protected void setAdapter(ViewPager viewPager) {

        TimespanPagerAdapter timespanAdapter = new SystemTimespanPagerAdapter(this, new UsageStatsWrapper(this));

        viewPager.setAdapter(timespanAdapter);
        viewPager.addOnPageChangeListener(timespanAdapter);

        viewPager.setPageMargin(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())
        );
        viewPager.setPageMarginDrawable(new ColorDrawable(getResources().getColor(R.color.page_switch_indicator)));
    }

    @Override
    protected void onReload(ViewPager viewPager, ProgressBar progressBar) {
        UsageStatsWrapper.flushCache();
        viewPager.getAdapter().notifyDataSetChanged();
    }
}
