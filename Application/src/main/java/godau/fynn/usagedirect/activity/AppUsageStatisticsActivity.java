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
import godau.fynn.librariesdirect.AboutDirectActivity;
import godau.fynn.librariesdirect.model.*;
import godau.fynn.usagedirect.BuildConfig;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.view.dialog.GrantPermissionDialog;
import godau.fynn.usagedirect.wrapper.UsageStatsManagerWrapper;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Shared code for both source flavors
 */
public abstract class AppUsageStatisticsActivity extends Activity {

    private ViewPager viewPager;
    private ProgressBar progressBar;
    private SmartTabLayout tabs;

    private Timer reloadTimer;
    private static final long RELOAD_INTERVAL = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabs = findViewById(R.id.viewpagertab);

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

            // Schedule reload
            reloadTimer = new Timer();
            reloadTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(AppUsageStatisticsActivity.this::reload);
                }
            }, RELOAD_INTERVAL, RELOAD_INTERVAL);
        }).start();


    }

    protected void reload() {
        onReload(viewPager, progressBar, () -> tabs.setViewPager(viewPager));
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:

                Intent intent = new AboutDirectActivity.IntentBuilder(this, R.string.app_name, BuildConfig.VERSION_NAME)
                        .setIcon(R.mipmap.ic_launcher)
                        .setAppDeveloperName("Fynn Godau")
                        .setAppDeveloperMastodon("https://fosstodon.org/@fynnDirect")
                        .setContent(new Object[]{
                                new Artwork(getString(R.string.icon), new License("CC BY-SA", null), null, "m4TZ", "https://social.anoxinon.de/@m4TZ"),
                                new Translator("Стоян", null, new Locale("bg")),
                                new Translator("dc7ia", null, new Locale("da")),
                                new Translator("mondstern", null, new Locale("da")),
                                new Translator("Lars Mühlbauer", null, new Locale("de")),
                                new Translator("Max Schallert", null, new Locale("de")),
                                new Translator("mondstern", null, new Locale("de")),
                                new Translator("Daniel Garcia Pallaviccini", null, new Locale("es")),
                                new Translator("Porrumentzio", null, new Locale("eu")),
                                new Translator("mondstern", null, new Locale("eu")),
                                new Translator("J. Lavoie", null, new Locale("fr")),
                                new Translator("eUgEntOptIc44", null, new Locale("fr")),
                                new Translator("jlemonde", null, new Locale("fr")),
                                new Translator("Xosé M", null, new Locale("gl")),
                                new Translator("liimee", null, new Locale("id")),
                                new Translator("mondstern", null, new Locale("id")),
                                new Translator("Giordano Scarso", null, new Locale("it")),
                                new Translator("Thomas Di Cristofaro", null, new Locale("it")),
                                new Translator("Allan Nordhøy", null, new Locale("nb")),
                                new Translator("dc7ia", null, new Locale("nb")),
                                new Translator("mondstern", null, new Locale("nb")),
                                new Translator("mondstern", null, new Locale("nl")),
                                new Translator("ewm", null, new Locale("pl")),
                                new Translator("André Marcelo Alvarenga", null, new Locale("pt", "br")),
                                new Translator("aevw", null, new Locale("pt", "br")),
                                new Translator("mondstern", null, new Locale("pt", "br")),
                                new Translator("Rikishi", null, new Locale("ru")),
                                new Translator("mondstern", null, new Locale("ru")),
                                new Translator("Hatsune Miku", null, new Locale("si")),
                                new Translator("dc7ia", null, new Locale("sv")),
                                new Translator("mondstern", null, new Locale("sv")),
                                new Translator("Naveen", null, new Locale("ta")),
                                new Translator("mondstern", null, new Locale("ta")),
                                new Translator("Quang Trung", null, new Locale("vi")),
                                new Translator("yeyuan98", null, new Locale("zh", "CN")),
                                new Library("PrettyTime", License.APACHE_20_LICENSE, null, "ocpsoft", "https://www.ocpsoft.org/prettytime/"),
                                new Library("SmartTabLayout", License.APACHE_20_LICENSE, null, "ogaclejapan", "https://github.com/ogaclejapan/SmartTabLayout"),
                                new Library("Pikolo", License.APACHE_20_LICENSE, null, "Madrapps", "https://github.com/Madrapps/Pikolo/"),
                                new Library("chartDirect", License.MIT_LICENSE, "The MIT License (MIT)\n" +
                                        "\n" +
                                        "Copyright (c) 2020 Fynn Godau\n" +
                                        "\n" +
                                        "Copyright (c) 2013 Ding Wenhao", "Fynn Godau and AndroidChart contributors", true, "https://codeberg.org/fynngodau/chartDirect"
                                ),
                                new Library("TypedRecyclerView", License.CC0_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/TypedRecyclerView"),
                                new Library("librariesDirect", License.CC0_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/librariesDirect"),
                                new Fork("AppUsageStatistics", License.APACHE_20_LICENSE, null, "AOSP", "https://github.com/googlesamples/android-AppUsageStatistics"),
                                new OwnLicense(License.GNU_GPL_V3_OR_LATER_LICENSE, null, "https://codeberg.org/fynngodau/usageDirect"),
                        })
                        .build();

                startActivity(intent);
                break;

            case R.id.menu_reload:
                reload();
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

    /**
     * @param then To be executed on the UI thread after reload is complete
     */
    protected abstract void onReload(ViewPager viewPager, ProgressBar progressBar, Runnable then);
}
