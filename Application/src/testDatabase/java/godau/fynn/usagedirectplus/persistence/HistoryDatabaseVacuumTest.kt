package godau.fynn.usagedirectplus.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HistoryDatabaseVacuumTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(HistoryDatabase.DATABASE_NAME)
        context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        context.deleteDatabase(HistoryDatabase.DATABASE_NAME)
        context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun freshInstallMarksCurrentVersionAsAlreadyVacuumed() {
        openDatabase().close()

        val lastVacuumedVersion = context
            .getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .getInt(HistoryDatabase.LAST_VACUUMED_VERSION_KEY, 0)

        assertThat(lastVacuumedVersion).isEqualTo(9)
    }

    @Test
    fun onOpenVacuumsWhenCurrentVersionWasNotVacuumedYet() {
        openDatabase().close()

        val dbFile = context.getDatabasePath(HistoryDatabase.DATABASE_NAME)
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { sqliteDb ->
            sqliteDb.execSQL("CREATE TABLE filler(data TEXT NOT NULL)")
            sqliteDb.beginTransaction()
            try {
                for (i in 0 until 5000) {
                    sqliteDb.execSQL(
                        "INSERT INTO filler(data) VALUES (?)",
                        arrayOf("x".repeat(2000))
                    )
                }
                sqliteDb.setTransactionSuccessful()
            } finally {
                sqliteDb.endTransaction()
            }

            sqliteDb.execSQL("DROP TABLE filler")
        }

        context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(HistoryDatabase.LAST_VACUUMED_VERSION_KEY, 7)
            .commit()

        openDatabase().close()

        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use { sqliteDb ->
            assertThat(queryString(sqliteDb, "PRAGMA integrity_check")).isEqualTo("ok")
        }

        val lastVacuumedVersion = context
            .getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .getInt(HistoryDatabase.LAST_VACUUMED_VERSION_KEY, 0)
        assertThat(lastVacuumedVersion).isEqualTo(9)
    }

    private fun queryLong(database: SQLiteDatabase, sql: String): Long {
        database.rawQuery(sql, null).use { cursor ->
            cursor.moveToFirst()
            return cursor.getLong(0)
        }
    }

    private fun queryString(database: SQLiteDatabase, sql: String): String {
        database.rawQuery(sql, null).use { cursor ->
            cursor.moveToFirst()
            return cursor.getString(0)
        }
    }

    private fun openDatabase(): HistoryDatabase {
        return HistoryDatabase.get(context).also {
            it.openHelper.writableDatabase
        }
    }
}
