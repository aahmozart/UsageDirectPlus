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

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirect.BuildConfig;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.persistence.EventLogRunnable;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.view.adapter.database.DatabaseTimespanPagerAdapter;
import godau.fynn.usagedirect.view.dialog.DatabaseDebugDialog;

/**
 * Different implementation of AUSA for the two source flavors
 */
public class SourceAppUsageStatisticsActivity extends AppUsageStatisticsActivity {

    private DatabaseTimespanPagerAdapter databaseTimespanPagerAdapter;

    private Object lastContextMenuTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_app_usage_statistics);

        databaseTimespanPagerAdapter = new DatabaseTimespanPagerAdapter(SourceAppUsageStatisticsActivity.this);

        super.onCreate(savedInstanceState);

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
        } else switch (item.getItemId()) {
            case R.id.menu_feedback:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_feedback)
                        .setMessage(R.string.feedback_message)
                        .setPositiveButton(R.string.menu_feedback, (dialog, which) ->
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_email)))))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;
            case R.id.menu_restore_hidden:
                new Thread(() -> {

                    HistoryDatabase database = HistoryDatabase.get(this);
                    int hiddenAmount = database.getUsageStatsDao().getHiddenAmount();

                    runOnUiThread(() -> new AlertDialog.Builder(this)
                            .setTitle(R.string.menu_restore_hidden)
                            .setMessage(
                                    getResources().getQuantityString(R.plurals.menu_restore_hidden_details,
                                            hiddenAmount, hiddenAmount)
                            )
                            .setPositiveButton(R.string.menu_restore_hidden_positive,
                                    (dialogInterface, i) -> new Thread(() -> {
                                        database.getUsageStatsDao().markUnhiddenAll();
                                        database.close();
                                        runOnUiThread(this::reload);
                                    }).start())
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> database.close())
                            .show()
                    );

                }).start();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            menu.add(R.string.menu_database);
        }

        new Thread(() -> {
            HistoryDatabase database = HistoryDatabase.get(this);
            boolean hasHidden = database.getUsageStatsDao().getHiddenAmount() > 0;
            runOnUiThread(() -> menu.findItem(R.id.menu_restore_hidden).setVisible(hasHidden));
        }).start();

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.usagestat_context_menu, menu);
        lastContextMenuTag = v.getTag();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_context_hide:
                SimpleUsageStat hideUsageStat = (SimpleUsageStat) lastContextMenuTag;
                new Thread(() -> {
                    HistoryDatabase database = HistoryDatabase.get(this);
                    database.getUsageStatsDao().markHidden(hideUsageStat);
                    database.close();

                    runOnUiThread(this::reload);
                }).start();

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
