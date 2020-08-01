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
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.room.Room;
import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.EventLogRunnable;
import godau.fynn.usagedirect.persistence.EventLogService;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.UsageStatsDao;
import godau.fynn.usagedirect.view.UsageStatBarView;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

import java.util.Map;

import static godau.fynn.usagedirect.persistence.HistoryDatabase.DATABASE_NAME;

public class DatabaseManagerActivity extends Activity {

    private UsageStatsDao usageStats;

    private TextView status;
    private Button insert;
    private UsageStatBarView barView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.title_feature_preview);

        setContentView(R.layout.activity_database);

        HistoryDatabase database = Room.databaseBuilder(this, HistoryDatabase.class, DATABASE_NAME).build();
        usageStats = database.getUsageStatsDao();

        status = findViewById(R.id.text_status);
        insert = findViewById(R.id.button_insert);
        barView = findViewById(R.id.bar_view);
        final CheckBox schedule = findViewById(R.id.button_schedule);

        final JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        boolean scheduled = scheduler.getAllPendingJobs().size() > 0;

        schedule.setChecked(scheduled);

        schedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    // Schedule job

                    JobInfo jobInfo = new JobInfo.Builder(
                            EventLogService.JOB_ID, new ComponentName(DatabaseManagerActivity.this, EventLogService.class)
                    )
                            .setPeriodic(24 * 60 * 60 * 1000)
                            .setPersisted(true)
                            .build();

                    int result = scheduler.schedule(jobInfo);

                    if (result == JobScheduler.RESULT_FAILURE) {
                        Toast.makeText(DatabaseManagerActivity.this, R.string.db_job_schedule_failure, Toast.LENGTH_SHORT).show();
                        schedule.setChecked(false);
                    }
                } else {

                    // Cancel job
                    scheduler.cancel(EventLogService.JOB_ID);
                }
            }
        });

        barView.setText(getString(R.string.db_chart_title));

        insert.setEnabled(false);


        updateViews();

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                status.setText(R.string.db_wait);
                insert.setEnabled(false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        new EventLogRunnable(DatabaseManagerActivity.this).run();
                        updateViews();

                    }
                }).start();
            }
        });

    }

    private void updateViews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int daysStored = usageStats.getDaysStoredAmount();
                final long totalHours = usageStats.getTotalTimeUsed() / 1000 / 60 / 60;

                final Map<Day, Long> map = usageStats.getTotalTimePerDay();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(getString(R.string.db_status, daysStored, totalHours));
                        insert.setEnabled(true);

                        barView.setData(map);

                    }
                });
            }
        }).start();
    }
}
