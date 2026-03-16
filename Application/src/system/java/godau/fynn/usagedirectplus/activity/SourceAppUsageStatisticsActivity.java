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

package godau.fynn.usagedirectplus.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.view.adapter.system.SystemTimespanPagerAdapter;
import godau.fynn.usagedirectplus.view.adapter.TimespanPagerAdapter;
import godau.fynn.usagedirectplus.view.dialog.GrantPermissionDialog;
import godau.fynn.usagedirectplus.wrapper.UsageStatsWrapper;

/**
 * Different implementation of AUSA for the two source flavors
 */
public class SourceAppUsageStatisticsActivity extends AppUsageStatisticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_system_stats);
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_system_stats);

        findViewById(R.id.system_stats_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SourceAppUsageStatisticsActivity.this, HelpActivity.class));
            }
        });
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
    protected void onReload(ViewPager viewPager, ProgressBar progressBar, Runnable then) {
        UsageStatsWrapper.flushCache();
        viewPager.getAdapter().notifyDataSetChanged();
        then.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        for (int i = 0; i < menu.size(); i++)  {
            MenuItem item = menu.getItem(i);

            if (item.getIcon() != null) {
                Drawable newIcon = item.getIcon();
                newIcon.mutate().setColorFilter(getThemeAccentColor(), PorterDuff.Mode.SRC_IN);
                item.setIcon(newIcon);
            }
        }
        return true;
    }

    /**
     * @return The accent color from the currently set theme
     */
    private int getThemeAccentColor() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorAccent, outValue, true);

        return outValue.data;
    }
}
