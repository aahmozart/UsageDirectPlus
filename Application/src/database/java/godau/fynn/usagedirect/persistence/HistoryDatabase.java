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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteDatabase;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.time.LocalDate;

/**
 * This database stores:
 * <p>usage stats: day | app id | times this app was used</p>
 *
 * <h5>Versions</h5>
 * <ul><b>1</b>: initial version</ul>
 * <ul><b>2</b>: <code>Day</code> object replaced with date integer</ul>
 *
 * <p>See also: <code>/Application/schemas</code></p>
 */
@Database(version = 2, entities = {SimpleUsageStat.class})
public abstract class HistoryDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "history";

    protected abstract UsageStatsDao getUsageStatsDao();

    public static UsageStatsDao getUsageStatsDao(Context context) {
        return Room.databaseBuilder(context, HistoryDatabase.class, DATABASE_NAME)
                .addMigrations(MIGRATION_DAY_TO_DATE)
                .build()
                .getUsageStatsDao();
    }

    private static final Migration MIGRATION_DAY_TO_DATE = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Create new table
            database.execSQL("CREATE TABLE mig_usageStats(day INTEGER NOT NULL, timeUsed INTEGER NOT NULL, applicationId TEXT NOT NULL, PRIMARY KEY(day, applicationId))");

            // Read existing data

            Cursor cursor = database.query("SELECT day, month, year, timeUsed, applicationId FROM usageStats");

            while (cursor.moveToNext()) {
                int day = cursor.getInt(0);
                int month = cursor.getInt(1);
                int year = cursor.getInt(2);
                long timeUsed = cursor.getLong(3);
                String applicationId = cursor.getString(4);

                // Calculate epoch day
                long date = LocalDate.of(year, month + 1, day)
                        .toEpochDay();

                // Insert into new table
                ContentValues values = new ContentValues(3);
                values.put("day", date);
                values.put("timeUsed", timeUsed);
                values.put("applicationId", applicationId);
                database.insert("mig_usageStats", SQLiteDatabase.CONFLICT_FAIL, values);

            }
            cursor.close();

            // Drop old table
            database.execSQL("DROP TABLE usageStats");

            // Rename new table
            database.execSQL("ALTER TABLE mig_usageStats RENAME TO usageStats");
        }
    };
}
