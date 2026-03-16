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

package godau.fynn.usagedirectplus.persistence;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class EventLogService extends JobService {

    public static final int JOB_ID = 3577; // EvLS on a numpad

    @Override
    public boolean onStartJob(final JobParameters params) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("EvLS", "The job is running");

                new EventLogRunnable(EventLogService.this).run();

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
