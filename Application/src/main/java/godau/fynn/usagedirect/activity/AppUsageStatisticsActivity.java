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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.librariesdirect.AboutLibrariesActivity;
import godau.fynn.librariesdirect.Library;
import godau.fynn.librariesdirect.License;
import godau.fynn.usagedirect.BuildConfig;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.view.dialog.GrantPermissionDialog;
import godau.fynn.usagedirect.wrapper.UsageStatsManagerWrapper;

/**
 * Shared code for both source flavors
 */
public abstract class AppUsageStatisticsActivity extends Activity {

    private ViewPager viewPager;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SmartTabLayout tabs = findViewById(R.id.viewpagertab);

        tabs.setElevation(getActionBar().getElevation());

        progressBar = findViewById(R.id.progress);
        viewPager = findViewById(R.id.viewpager);

        if (!UsageStatsManagerWrapper.isPermissionGranted(this)) {
            new GrantPermissionDialog(this).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {

            prepare();

            runOnUiThread(() -> {

                setAdapter(viewPager);

                viewPager.setOffscreenPageLimit(3);

                tabs.setViewPager(viewPager);
                progressBar.setVisibility(View.GONE);
            });
        }).start();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:

                Intent intent = new AboutLibrariesActivity.IntentBuilder(this)
                        .setLibraries(new Library[]{
                                new Library(getString(R.string.app_name) + ' ' + BuildConfig.VERSION_NAME, License.GNU_GPL_V3_OR_LATER_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/usageDirect"),
                            new Library("AppUsageStatistics", License.APACHE_20_LICENSE, null, "The Android Open Source Project, Inc", "https://github.com/googlesamples/android-AppUsageStatistics"),
                            new Library("AndroidCharts", License.MIT_LICENSE, "The MIT License (MIT)\n" +
                                    "\n" +
                                    "Copyright (c) 2013 Ding Wenhao", "Ding Wenhao", "https://github.com/HackPlan/AndroidCharts"),
                            new Library("Humanize", License.APACHE_20_LICENSE, null, "mfornos", "http://mfornos.github.io/humanize/"),
                            new Library("SmartTabLayout", License.APACHE_20_LICENSE, null, "ogaclejapan", "https://github.com/ogaclejapan/SmartTabLayout"),
                            new Library("librariesDirect", License.CC0_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/librariesDirect"),
                        })
                        .setHeaderText(getString(R.string.about_libraries_header, getString(R.string.app_name)))
                        .build();

                startActivity(intent);
                break;

            case R.id.menu_reload:
                onReload(viewPager, progressBar);
                break;

            case R.id.menu_charts:
                startActivity(new Intent(this, ChartsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GrantPermissionDialog.REQUEST_CODE)
            recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Not on the main thread. Executed before {@link #setAdapter(ViewPager)} is called.
     */
    protected abstract void prepare();

    /**
     * Responsible for setting an adapter to the passed view pager and possibly
     * configuring it further.
     *
     * @param viewPager ViewPager to configure
     */
    protected abstract void setAdapter(ViewPager viewPager);

    protected abstract void onReload(ViewPager viewPager, ProgressBar progressBar);
}
