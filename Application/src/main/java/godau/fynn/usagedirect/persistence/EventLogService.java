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

package godau.fynn.usagedirect.persistence;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import androidx.room.Room;
import godau.fynn.usagedirect.wrapper.UsageStatsWrapper;

import static godau.fynn.usagedirect.persistence.HistoryDatabase.DATABASE_NAME;

public class EventLogService extends JobService {

    public static final int JOB_ID = 3577; // EvL on a numpad

    @Override
    public boolean onStartJob(final JobParameters params) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("EvLS", "The job is running");

                HistoryDatabase database = Room.databaseBuilder(EventLogService.this, HistoryDatabase.class, DATABASE_NAME).build();
                UsageStatsDao usageStats = database.getUsageStatsDao();

                usageStats.insert(
                        new UsageStatsWrapper(EventLogService.this).getAllSimpleUsageStats()
                );

                jobFinished(params, false);

            }
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
