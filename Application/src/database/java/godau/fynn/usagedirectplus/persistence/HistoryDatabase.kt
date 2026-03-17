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
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.charts.WeeklyAverageBarChart
import java.time.LocalDate

@Database(
    version = 6,
    entities = [SimpleUsageStat::class, LastUsedStat::class, AppColor::class, UsageInterval::class, ScreenEvent::class]
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

        @JvmStatic
        fun get(context: Context): HistoryDatabase {
            return Room.databaseBuilder(context, HistoryDatabase::class.java, DATABASE_NAME)
                .addMigrations(
                    MIGRATION_DAY_TO_DATE,
                    MIGRATION_ADD_LAST_USED,
                    MIGRATION_ADD_HIDDEN_FLAG,
                    MIGRATION_ADD_COLORS,
                    MIGRATION_ADD_INTERVALS_AND_SCREEN_EVENTS
                )
                .build()
        }

        private val MIGRATION_DAY_TO_DATE = object : Migration(1, 2) {
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

        private val MIGRATION_ADD_LAST_USED = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 2 → 3: creating last used table")
                database.execSQL("CREATE TABLE `lastUsed` (`applicationId` TEXT NOT NULL, `lastUsed` INTEGER NOT NULL, PRIMARY KEY(`applicationId`))")
            }
        }

        private val MIGRATION_ADD_HIDDEN_FLAG = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 3 → 4: adding hidden flag to usage stats table")
                database.execSQL("ALTER TABLE usageStats ADD COLUMN `hidden` INTEGER NOT NULL DEFAULT(0)")
            }
        }

        private val MIGRATION_ADD_COLORS = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 4 → 5: creating app color table")
                database.execSQL("CREATE TABLE IF NOT EXISTS `colors` (`applicationId` TEXT NOT NULL, `color` INTEGER NOT NULL, `priority` INTEGER NOT NULL, PRIMARY KEY(`applicationId`))")
            }
        }

        private val MIGRATION_ADD_INTERVALS_AND_SCREEN_EVENTS = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("HistoryDatabase", "Migration 5 → 6: creating usage intervals and screen events tables")
                database.execSQL("CREATE TABLE IF NOT EXISTS `usageIntervals` (`beginTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `applicationId` TEXT NOT NULL, PRIMARY KEY(`beginTime`, `applicationId`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_usageIntervals_applicationId` ON `usageIntervals` (`applicationId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_usageIntervals_beginTime` ON `usageIntervals` (`beginTime`)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `screenEvents` (`timestamp` INTEGER NOT NULL, `eventType` INTEGER NOT NULL, PRIMARY KEY(`timestamp`))")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_screenEvents_timestamp` ON `screenEvents` (`timestamp`)")
            }
        }
    }
}
