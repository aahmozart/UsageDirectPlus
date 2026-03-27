package godau.fynn.usagedirectplus.persistence

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.SimpleUsageStat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        requireNotNull(HistoryDatabase::class.java.canonicalName),
        FrameworkSQLiteOpenHelperFactory()
    )

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "history-migration-test"

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrate6To7_normalizesAppIdsAndKeepsDataAccessible() {
        helper.createDatabase(databaseName, 6).apply {
            execSQL("INSERT INTO usageStats(day, timeUsed, applicationId, hidden) VALUES (1, 100, 'com.a', 0)")
            execSQL("INSERT INTO usageStats(day, timeUsed, applicationId, hidden) VALUES (1, 75, 'com.b', 1)")
            execSQL("INSERT INTO usageStats(day, timeUsed, applicationId, hidden) VALUES (2, 250, 'com.only.stats', 0)")
            execSQL("INSERT INTO lastUsed(applicationId, lastUsed) VALUES ('com.a', 111)")
            execSQL("INSERT INTO lastUsed(applicationId, lastUsed) VALUES ('com.only.last', 222)")
            execSQL("INSERT INTO colors(applicationId, color, priority) VALUES ('com.a', 123, 2)")
            execSQL("INSERT INTO colors(applicationId, color, priority) VALUES ('com.only.color', 456, 1)")
            execSQL("INSERT INTO usageIntervals(beginTime, endTime, applicationId) VALUES (1000, 2000, 'com.a')")
            execSQL("INSERT INTO usageIntervals(beginTime, endTime, applicationId) VALUES (3000, 5000, 'com.a')")
            execSQL("INSERT INTO usageIntervals(beginTime, endTime, applicationId) VALUES (4000, 6000, 'com.b')")
            execSQL("INSERT INTO screenEvents(timestamp, eventType) VALUES (1000, 17)")
            execSQL("INSERT INTO screenEvents(timestamp, eventType) VALUES (2000, 18)")
            close()
        }

        helper.runMigrationsAndValidate(databaseName, 7, true, *HistoryDatabase.ALL_MIGRATIONS)

        val db = Room.databaseBuilder(context, HistoryDatabase::class.java, databaseName)
            .addMigrations(*HistoryDatabase.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()

        try {
            assertThat(db.getUsageIntervalDao().getByApp("com.a").map(UsageInterval::beginTime))
                .containsExactly(1000L, 3000L).inOrder()
            assertThat(db.getUsageStatsDao().getUsageStats().map(SimpleUsageStat::applicationId))
                .containsExactly("com.a", "com.only.stats").inOrder()
            assertThat(db.getLastUsedDao().getLastUsedStats().toList())
                .containsExactly(
                    LastUsedStat("com.a", 111),
                    LastUsedStat("com.only.last", 222)
                )
            assertThat(db.getAppColorDao().getAppColor("com.only.color"))
                .isEqualTo(AppColor("com.only.color", 456, 1))
            assertThat(db.getScreenEventDao().getUnlockCount(0, 5_000)).isEqualTo(1)

            val writableDatabase = db.openHelper.writableDatabase
            assertThat(queryLong(writableDatabase, "SELECT COUNT(*) FROM apps")).isEqualTo(5)
            assertThat(queryLong(writableDatabase, "SELECT COUNT(*) FROM usageStats")).isEqualTo(3)
            assertThat(queryLong(writableDatabase, "SELECT COUNT(*) FROM lastUsed")).isEqualTo(2)
            assertThat(queryLong(writableDatabase, "SELECT COUNT(*) FROM colors")).isEqualTo(2)
            assertThat(queryLong(writableDatabase, "SELECT COUNT(*) FROM usageIntervals")).isEqualTo(3)

            val usageIntervalIndexes = querySecondColumnStrings(
                writableDatabase,
                "PRAGMA index_list(`usageIntervals`)"
            )
            assertThat(usageIntervalIndexes).contains("index_usageIntervals_appId")
            assertThat(usageIntervalIndexes).doesNotContain("index_usageIntervals_beginTime")
            assertThat(usageIntervalIndexes).doesNotContain("index_usageIntervals_applicationId")

            val screenEventIndexes = querySecondColumnStrings(
                writableDatabase,
                "PRAGMA index_list(`screenEvents`)"
            )
            assertThat(screenEventIndexes).doesNotContain("index_screenEvents_timestamp")
        } finally {
            db.close()
        }
    }

    @Test
    fun migrate6To7_handlesEmptyColorsTable() {
        helper.createDatabase(databaseName, 6).apply {
            execSQL("INSERT INTO usageStats(day, timeUsed, applicationId, hidden) VALUES (1, 100, 'com.a', 0)")
            execSQL("INSERT INTO lastUsed(applicationId, lastUsed) VALUES ('com.a', 111)")
            execSQL("INSERT INTO usageIntervals(beginTime, endTime, applicationId) VALUES (1000, 2000, 'com.a')")
            close()
        }

        helper.runMigrationsAndValidate(databaseName, 7, true, *HistoryDatabase.ALL_MIGRATIONS)

        val db = Room.databaseBuilder(context, HistoryDatabase::class.java, databaseName)
            .addMigrations(*HistoryDatabase.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()

        try {
            assertThat(db.getAppColorDao().getAppColorMap()).isEmpty()
            assertThat(db.getUsageIntervalDao().getByTimeRange(0L, 5_000L))
                .containsExactly(UsageInterval(1000L, 2000L, "com.a"))
            assertThat(db.getLastUsedDao().getLastUsedStats().toList())
                .containsExactly(LastUsedStat("com.a", 111))
        } finally {
            db.close()
        }
    }

    @Test
    fun migrate7To8_createsBrowserTabSessionsTableAndKeepsExistingDataAccessible() {
        helper.createDatabase(databaseName, 7).apply {
            execSQL("INSERT INTO apps(id, applicationId) VALUES (1, 'com.android.chrome')")
            execSQL("INSERT INTO usageIntervals(beginTime, endTime, appId) VALUES (1000, 2000, 1)")
            close()
        }

        helper.runMigrationsAndValidate(databaseName, 8, true, *HistoryDatabase.ALL_MIGRATIONS)

        val db = Room.databaseBuilder(context, HistoryDatabase::class.java, databaseName)
            .addMigrations(*HistoryDatabase.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()

        try {
            assertThat(db.getUsageIntervalDao().getByApp("com.android.chrome"))
                .containsExactly(UsageInterval(1000L, 2000L, "com.android.chrome"))

            val browserSessionId = db.getBrowserTabSessionDao().insertOpenSession(
                applicationId = "com.android.chrome",
                openedAt = 3000L,
                title = "Example Domain",
                url = "example.com",
                privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
            )
            assertThat(browserSessionId).isGreaterThan(0L)

            val writableDatabase = db.openHelper.writableDatabase
            val browserIndexes = querySecondColumnStrings(
                writableDatabase,
                "PRAGMA index_list(`browserTabSessions`)"
            )
            assertThat(browserIndexes).contains("index_browserTabSessions_appId")
            assertThat(browserIndexes).contains("index_browserTabSessions_openedAt")
            assertThat(browserIndexes).contains("index_browserTabSessions_appId_openedAt")
        } finally {
            db.close()
        }
    }

    private fun queryLong(database: SupportSQLiteDatabase, sql: String): Long {
        database.query(sql).use { cursor ->
            cursor.moveToFirst()
            return cursor.getLong(0)
        }
    }

    private fun querySecondColumnStrings(database: SupportSQLiteDatabase, sql: String): List<String> {
        database.query(sql).use { cursor ->
            val results = mutableListOf<String>()
            while (cursor.moveToNext()) {
                results.add(cursor.getString(1))
            }
            return results
        }
    }
}
