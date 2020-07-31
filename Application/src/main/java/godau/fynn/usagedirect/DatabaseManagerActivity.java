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
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.room.Room;
import godau.fynn.usagedirect.persistence.EventLogService;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.UsageStatsDao;
import godau.fynn.usagedirect.view.FramedBarView;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static godau.fynn.usagedirect.persistence.HistoryDatabase.DATABASE_NAME;

public class DatabaseManagerActivity extends Activity {

    private UsageStatsDao usageStats;

    private TextView status;
    private Button insert;
    private FramedBarView barView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.title_feature_preview);

        setContentView(R.layout.activity_database);

        HistoryDatabase database = Room.databaseBuilder(this, HistoryDatabase.class, DATABASE_NAME).build();
        usageStats = database.getUsageStatsDao();

        final UsageStatsWrapper usageStatsWrapper = new UsageStatsWrapper(this);

        status = findViewById(R.id.text_status);
        insert = findViewById(R.id.button_insert);
        barView = findViewById(R.id.bar_view);
        final CheckBox schedule = findViewById(R.id.button_schedule);

        final JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        boolean scheduled = scheduler.getPendingJob(EventLogService.JOB_ID) != null;

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
        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




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

                        usageStats.insert(
                                usageStatsWrapper.getAllSimpleUsageStats()
                        );

                        updateViews();

                    }
                }).start();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();


    }

    private void updateViews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int daysStored = usageStats.getDaysStoredAmount();
                final long totalHours = usageStats.getTotalTimeUsed() / 1000 / 60 / 60;

                Map<Day, Long> map = usageStats.getTotalTimePerDay();

                final List<String> labels = new ArrayList<>();
                final List<Integer> data = new ArrayList<>();
                for (Day d : map.keySet()) {
                    labels.add(String.valueOf(d.day));
                    int seconds = (int) (map.get(d) / 1000);
                    data.add(seconds);
                }

                int max = Collections.max(data);
                final int chartMax = max + (30 * 60);

                // Calculate vertical line frequency
                int maxHours = (max / 60 / 60) + 1;
                int frequency = 1;
                while (maxHours / 15 > frequency) {
                    frequency *= 10;
                }

                // Add lines
                final List<Integer> lines = new ArrayList<>();
                final List<String> lineLabels = new ArrayList<>();
                int counter = frequency;
                do {
                    lines.add(counter * 60 * 60);
                    lineLabels.add(String.valueOf(counter));
                } while ((counter += frequency) < maxHours);

                // Don't display more than 4 vertical line labels
                if (lineLabels.size() > 4) {
                    lineLabels.clear();
                    lineLabels.add(String.valueOf(frequency));
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(getString(R.string.db_status, daysStored, totalHours));
                        insert.setEnabled(true);

                        barView.getBarView().setDataList(data, chartMax);
                        barView.getBarView().setBottomTextList(labels);
                        barView.getBarView().setVerticalLines(lines, chartMax);
                        barView.getBarView().setVerticalLineLabels(lineLabels);

                    }
                });
            }
        }).start();
    }
}
