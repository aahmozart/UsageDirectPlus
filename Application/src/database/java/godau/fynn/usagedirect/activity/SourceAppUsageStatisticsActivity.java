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
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import godau.fynn.usagedirect.DebugMenu;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.persistence.EventLogRunnable;
import godau.fynn.usagedirect.persistence.Export;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.view.adapter.UsageListViewPagerAdapter;
import godau.fynn.usagedirect.view.adapter.database.DatabaseTimespanPagerAdapter;

import java.io.IOException;

/**
 * Different implementation of AUSA for the two source flavors
 */
public class SourceAppUsageStatisticsActivity extends AppUsageStatisticsActivity {

    private DatabaseTimespanPagerAdapter databaseTimespanPagerAdapter;

    private Object lastContextMenuTag;

    private static final int REQUEST_EXPORT_PICK_FILE = 397078; // EXP0RT
    private static final int REQUEST_COLOR_ACTIVITY = 20507; // C0L0R

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
    protected void onReload(final ViewPager viewPager, final ProgressBar progressBar, final Runnable then) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            prepare();

            runOnUiThread(() -> {
                databaseTimespanPagerAdapter.notifyDataSetChanged();
                viewPager.getAdapter().notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);

                then.run();
            });
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (DebugMenu.onOptionsItemSelected(item, this)) {
            return true;
        } else switch (item.getItemId()) {
            case R.id.menu_color:
                startActivityForResult(new Intent(this, ColorActivity.class), REQUEST_COLOR_ACTIVITY);
                break;
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
            case R.id.menu_export_database:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.export_title)
                        .setMessage(R.string.export_message)
                        .setPositiveButton(R.string.go, (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("application/vnd.sqlite3");
                            intent.putExtra(Intent.EXTRA_TITLE, "usageDirect-history.sqlite3");
                            startActivityForResult(intent, REQUEST_EXPORT_PICK_FILE);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DebugMenu.addTo(menu);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_EXPORT_PICK_FILE
                && resultCode == Activity.RESULT_OK
                && data != null
        ) {
            try {
                Export.exportHistoryDatabase(data, this);
            } catch (IOException e) {
                Toast.makeText(this, R.string.export_io_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_COLOR_ACTIVITY
                && resultCode == RESULT_OK
        ) {
            reload();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
