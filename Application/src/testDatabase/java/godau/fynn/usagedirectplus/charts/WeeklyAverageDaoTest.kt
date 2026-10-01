package godau.fynn.usagedirectplus.charts

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.persistence.AppColor
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.DayOfWeek
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class WeeklyAverageDaoTest {

    private lateinit var db: HistoryDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * A colored app with usage on more than one day used to be returned once per
     * usage row by the colors query, which crashed the chart with a NullPointerException.
     */
    @Test
    fun getData_coloredAppUsedOnSeveralDays_doesNotCrash() {
        val monday = LocalDate.of(2024, 1, 1)
        assertThat(monday.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)

        db.getUsageStatsDao().insert(
            listOf(
                SimpleUsageStat(monday.toEpochDay(), 60_000L, "com.a", false),
                SimpleUsageStat(monday.plusWeeks(1).toEpochDay(), 180_000L, "com.a", false),
                SimpleUsageStat(monday.toEpochDay(), 30_000L, "com.b", false)
            )
        )
        db.getAppColorDao().insert(arrayOf(AppColor("com.a", 0xFF0000, 0)))

        val values = db.getWeeklyDao().getData()

        assertThat(values).hasLength(DayOfWeek.values().size)
        // com.a averages 120 s over two Mondays; uncolored com.b averages 15 s
        assertThat(values[DayOfWeek.MONDAY.ordinal].value).isEqualTo(135)
        assertThat(values[DayOfWeek.TUESDAY.ordinal].value).isEqualTo(0)
    }
}
