package godau.fynn.usagedirectplus.persistence

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import godau.fynn.usagedirectplus.charts.WeeklyAverageBarChart
import java.time.LocalDate

@Database(
    version = 7,
    entities = [
        StoredUsageStat::class,
        StoredLastUsedStat::class,
        StoredAppColor::class,
        StoredUsageInterval::class,
        StoredApp::class,
        ScreenEvent::class
    ]
)
abstract class HistoryDatabase : RoomDatabase() {

    abstract fun getUsageStatsDao(): UsageStatsDao
    abstract fun getLastUsedDao(): LastUsedDao
    abstract fun getAppColorDao(): AppColorDao
    abstract fun getWeeklyDao(): WeeklyAverageBarChart.WeeklyAverageDao
    abstract fun getUsageIntervalDao(): UsageIntervalDao
    abstract fun getScreenEventDao(): ScreenEventDao

    companion object {
        const val DATABASE_NAME = "history"
        internal const val LAST_VACUUMED_VERSION_KEY = "lastVacuumedVersion"

        @JvmStatic
        fun get(context: Context): HistoryDatabase {
            val appContext = context.applicationContext
            return Room.databaseBuilder(appContext, HistoryDatabase::class.java, DATABASE_NAME)
                .addMigrations(
                    MIGRATION_DAY_TO_DATE,
                    MIGRATION_ADD_LAST_USED,
                    MIGRATION_ADD_HIDDEN_FLAG,
                    MIGRATION_ADD_COLORS,
                    MIGRATION_ADD_INTERVALS_AND_SCREEN_EVENTS,
                    MIGRATION_NORMALIZE_APP_IDS
                )
                .addCallback(getVacuumCallback(appContext))
                .build()
        }

        private fun getVacuumCallback(context: Context) = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                getPreferences(context).edit()
                    .putInt(LAST_VACUUMED_VERSION_KEY, getUserVersion(db))
                    .apply()
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

                val currentVersion = getUserVersion(db)
                val prefs = getPreferences(context)
                val lastVacuumedVersion = prefs.getInt(LAST_VACUUMED_VERSION_KEY, 0)

                if (lastVacuumedVersion >= currentVersion) {
                    return
                }

                try {
                    Log.d(
                        "HistoryDatabase",
                        "Running VACUUM for database version $currentVersion after last vacuumed version $lastVacuumedVersion"
                    )
                    db.execSQL("VACUUM")
                    prefs.edit()
                        .putInt(LAST_VACUUMED_VERSION_KEY, currentVersion)
                        .apply()
                } catch (exception: Exception) {
                    Log.w("HistoryDatabase", "VACUUM after database upgrade failed", exception)
                }
            }
        }

        private fun getPreferences(context: Context) =
            context.getSharedPreferences(DATABASE_NAME, Context.MODE_PRIVATE)

        private fun getUserVersion(database: SupportSQLiteDatabase): Int {
            database.query("PRAGMA user_version").use { cursor ->
                return if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
        }

        internal val MIGRATION_DAY_TO_DATE = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 1 → 2")

                database.execSQL("CREATE TABLE mig_usageStats(day INTEGER NOT NULL, timeUsed INTEGER NOT NULL, applicationId TEXT NOT NULL, PRIMARY KEY(day, applicationId))")

                val cursor = database.query("SELECT day, month, year, timeUsed, applicationId FROM usageStats")
                while (cursor.moveToNext()) {
                    val day = cursor.getInt(0)
                    val month = cursor.getInt(1)
                    val year = cursor.getInt(2)
                    val timeUsed = cursor.getLong(3)
                    val applicationId = cursor.getString(4)

                    val date = LocalDate.of(year, month + 1, day).toEpochDay()

                    val values = ContentValues(3)
                    values.put("day", date)
                    values.put("timeUsed", timeUsed)
                    values.put("applicationId", applicationId)
                    database.insert("mig_usageStats", SQLiteDatabase.CONFLICT_NONE, values)
                }
                cursor.close()

                database.execSQL("DROP TABLE usageStats")
                database.execSQL("ALTER TABLE mig_usageStats RENAME TO usageStats")
            }
        }

        internal val MIGRATION_ADD_LAST_USED = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 2 → 3: creating last used table")
                database.execSQL("CREATE TABLE `lastUsed` (`applicationId` TEXT NOT NULL, `lastUsed` INTEGER NOT NULL, PRIMARY KEY(`applicationId`))")
            }
        }

        internal val MIGRATION_ADD_HIDDEN_FLAG = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 3 → 4: adding hidden flag to usage stats table")
                database.execSQL("ALTER TABLE usageStats ADD COLUMN `hidden` INTEGER NOT NULL DEFAULT(0)")
            }
        }

        internal val MIGRATION_ADD_COLORS = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 4 → 5: creating app color table")
                database.execSQL("CREATE TABLE IF NOT EXISTS `colors` (`applicationId` TEXT NOT NULL, `color` INTEGER NOT NULL, `priority` INTEGER NOT NULL, PRIMARY KEY(`applicationId`))")
            }
        }

        internal val MIGRATION_ADD_INTERVALS_AND_SCREEN_EVENTS = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 5 → 6: creating usage intervals and screen events tables")
                database.execSQL("CREATE TABLE IF NOT EXISTS `usageIntervals` (`beginTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `applicationId` TEXT NOT NULL, PRIMARY KEY(`beginTime`, `applicationId`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_usageIntervals_applicationId` ON `usageIntervals` (`applicationId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_usageIntervals_beginTime` ON `usageIntervals` (`beginTime`)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `screenEvents` (`timestamp` INTEGER NOT NULL, `eventType` INTEGER NOT NULL, PRIMARY KEY(`timestamp`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_screenEvents_timestamp` ON `screenEvents` (`timestamp`)")
            }
        }

        internal val MIGRATION_NORMALIZE_APP_IDS = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 6 → 7: normalizing app ids")

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `apps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `applicationId` TEXT NOT NULL)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_apps_applicationId` ON `apps` (`applicationId`)"
                )
                database.execSQL(
                    "INSERT OR IGNORE INTO `apps` (`applicationId`) " +
                        "SELECT `applicationId` FROM `usageIntervals` " +
                        "UNION SELECT `applicationId` FROM `usageStats` " +
                        "UNION SELECT `applicationId` FROM `lastUsed` " +
                        "UNION SELECT `applicationId` FROM `colors`"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `usageStats_new` (" +
                        "`day` INTEGER NOT NULL, " +
                        "`timeUsed` INTEGER NOT NULL, " +
                        "`appId` INTEGER NOT NULL, " +
                        "`hidden` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`day`, `appId`))"
                )
                database.execSQL(
                    "INSERT INTO `usageStats_new` (`day`, `timeUsed`, `appId`, `hidden`) " +
                        "SELECT `usageStats`.`day`, `usageStats`.`timeUsed`, `apps`.`id`, `usageStats`.`hidden` " +
                        "FROM `usageStats` " +
                        "INNER JOIN `apps` ON `apps`.`applicationId` = `usageStats`.`applicationId`"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `lastUsed_new` (" +
                        "`appId` INTEGER NOT NULL, " +
                        "`lastUsed` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`appId`))"
                )
                database.execSQL(
                    "INSERT INTO `lastUsed_new` (`appId`, `lastUsed`) " +
                        "SELECT `apps`.`id`, `lastUsed`.`lastUsed` " +
                        "FROM `lastUsed` " +
                        "INNER JOIN `apps` ON `apps`.`applicationId` = `lastUsed`.`applicationId`"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `colors_new` (" +
                        "`appId` INTEGER NOT NULL, " +
                        "`color` INTEGER NOT NULL, " +
                        "`priority` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`appId`))"
                )
                database.execSQL(
                    "INSERT INTO `colors_new` (`appId`, `color`, `priority`) " +
                        "SELECT `apps`.`id`, `colors`.`color`, `colors`.`priority` " +
                        "FROM `colors` " +
                        "INNER JOIN `apps` ON `apps`.`applicationId` = `colors`.`applicationId`"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `usageIntervals_new` (" +
                        "`beginTime` INTEGER NOT NULL, " +
                        "`endTime` INTEGER NOT NULL, " +
                        "`appId` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`beginTime`, `appId`))"
                )
                database.execSQL(
                    "INSERT INTO `usageIntervals_new` (`beginTime`, `endTime`, `appId`) " +
                        "SELECT `usageIntervals`.`beginTime`, `usageIntervals`.`endTime`, `apps`.`id` " +
                        "FROM `usageIntervals` " +
                        "INNER JOIN `apps` ON `apps`.`applicationId` = `usageIntervals`.`applicationId`"
                )

                database.execSQL("DROP TABLE `usageStats`")
                database.execSQL("DROP TABLE `lastUsed`")
                database.execSQL("DROP TABLE `colors`")
                database.execSQL("DROP TABLE `usageIntervals`")

                database.execSQL("ALTER TABLE `usageStats_new` RENAME TO `usageStats`")
                database.execSQL("ALTER TABLE `lastUsed_new` RENAME TO `lastUsed`")
                database.execSQL("ALTER TABLE `colors_new` RENAME TO `colors`")
                database.execSQL("ALTER TABLE `usageIntervals_new` RENAME TO `usageIntervals`")

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_usageIntervals_appId` ON `usageIntervals` (`appId`)"
                )
                database.execSQL("DROP INDEX IF EXISTS `index_screenEvents_timestamp`")
            }
        }

        internal val ALL_MIGRATIONS = arrayOf(
            MIGRATION_DAY_TO_DATE,
            MIGRATION_ADD_LAST_USED,
            MIGRATION_ADD_HIDDEN_FLAG,
            MIGRATION_ADD_COLORS,
            MIGRATION_ADD_INTERVALS_AND_SCREEN_EVENTS,
            MIGRATION_NORMALIZE_APP_IDS
        )
    }
}
