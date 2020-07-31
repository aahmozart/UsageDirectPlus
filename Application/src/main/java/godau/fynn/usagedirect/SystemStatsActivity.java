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
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.usagedirect.view.SystemTimespanPagerAdapter;
import godau.fynn.usagedirect.view.TimespanPagerAdapter;
import godau.fynn.usagedirect.view.dialog.GrantPermissionDialog;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

public class SystemStatsActivity extends Activity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_stats);

        /*findViewById(R.id.system_stats_warning_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.system_stats_warning).setVisibility(View.GONE);
            }
        });*/

        SmartTabLayout tabs = findViewById(R.id.viewpagertab);

        tabs.setElevation(getActionBar().getElevation());

        getActionBar().setTitle(R.string.title_system_stats);
        getActionBar().setElevation(0f);

        final UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(SystemStatsActivity.this);

        if (!usageStatsWrapper.isPermissionGranted()) {
            new GrantPermissionDialog(this).show();
        }

        final TimespanPagerAdapter timespanAdapter = new SystemTimespanPagerAdapter(this, usageStatsWrapper);

        viewPager = findViewById(R.id.timespanpager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(timespanAdapter);

        viewPager.addOnPageChangeListener(timespanAdapter);

        viewPager.setPageMargin(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())
        );
        viewPager.setPageMarginDrawable(new ColorDrawable(getColor(R.color.page_switch_indicator)));

        tabs.setViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_reload:
                UsageStatsWrapper.flushCache();
                viewPager.getAdapter().notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_system_stats, menu);
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
