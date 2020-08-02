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

import androidx.room.Database;
import androidx.room.RoomDatabase;
import godau.fynn.usagedirect.SimpleUsageStat;

/**
 * This database stores:
 * usage stats: day | app id | times this app was used
 */
@Database(version = 1, entities = {SimpleUsageStat.class})
public abstract class HistoryDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "history";

    public abstract UsageStatsDao getUsageStatsDao();

}
