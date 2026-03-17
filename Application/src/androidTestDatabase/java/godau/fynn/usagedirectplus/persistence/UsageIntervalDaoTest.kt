package godau.fynn.usagedirectplus.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.runner.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class UsageIntervalDaoTest {

    private lateinit var db: HistoryDatabase
    private lateinit var dao: UsageIntervalDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.getUsageIntervalDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertNonOverlappingInsertsNonOverlapping() {
        val intervals = listOf(
            UsageInterval(1000L, 2000L, "com.a"),
            UsageInterval(3000L, 4000L, "com.a")
        )

        dao.insertNonOverlapping(intervals)

        val result = dao.getByTimeRange(0L, 5000L)
        assertThat(result).hasSize(2)
    }

    @Test
    fun insertNonOverlappingSkipsOverlappingForSameApp() {
        dao.insertNonOverlapping(listOf(
            UsageInterval(1000L, 3000L, "com.a")
        ))

        // This overlaps with the existing interval for the same app
        dao.insertNonOverlapping(listOf(
            UsageInterval(2000L, 4000L, "com.a")
        ))

        val result = dao.getByTimeRange(0L, 5000L)
        assertThat(result).hasSize(1)
        assertThat(result[0].beginTime).isEqualTo(1000L)
    }

    @Test
    fun insertNonOverlappingAllowsOverlappingForDifferentApps() {
        dao.insertNonOverlapping(listOf(
            UsageInterval(1000L, 3000L, "com.a")
        ))

        // Same time range, different app
        dao.insertNonOverlapping(listOf(
            UsageInterval(2000L, 4000L, "com.b")
        ))

        val result = dao.getByTimeRange(0L, 5000L)
        assertThat(result).hasSize(2)
    }

    @Test
    fun getByTimeRangeFiltersCorrectly() {
        dao.insertNonOverlapping(listOf(
            UsageInterval(1000L, 2000L, "com.a"),
            UsageInterval(3000L, 4000L, "com.b"),
            UsageInterval(5000L, 6000L, "com.c")
        ))

        val result = dao.getByTimeRange(2500L, 4500L)
        assertThat(result).hasSize(1)
        assertThat(result[0].applicationId).isEqualTo("com.b")
    }

    @Test
    fun getByAppFiltersCorrectly() {
        dao.insertNonOverlapping(listOf(
            UsageInterval(1000L, 2000L, "com.a"),
            UsageInterval(3000L, 4000L, "com.b"),
            UsageInterval(5000L, 6000L, "com.a")
        ))

        val result = dao.getByApp("com.a")
        assertThat(result).hasSize(2)
        assertThat(result.map { it.applicationId }).containsExactly("com.a", "com.a")
    }

    @Test
    fun getByAppAndTimeRangeFiltersOnBoth() {
        dao.insertNonOverlapping(listOf(
            UsageInterval(1000L, 2000L, "com.a"),
            UsageInterval(3000L, 4000L, "com.a"),
            UsageInterval(5000L, 6000L, "com.a"),
            UsageInterval(3000L, 4000L, "com.b")
        ))

        val result = dao.getByAppAndTimeRange("com.a", 2500L, 4500L)
        assertThat(result).hasSize(1)
        assertThat(result[0].beginTime).isEqualTo(3000L)
    }

    @Test
    fun getLatestEndTime() {
        dao.insertNonOverlapping(listOf(
            UsageInterval(1000L, 2000L, "com.a"),
            UsageInterval(3000L, 9000L, "com.b"),
            UsageInterval(5000L, 6000L, "com.c")
        ))

        assertThat(dao.getLatestEndTime()).isEqualTo(9000L)
    }

    @Test
    fun insertNonOverlappingEmptyListDoesNothing() {
        dao.insertNonOverlapping(emptyList())

        val result = dao.getByTimeRange(0L, Long.MAX_VALUE)
        assertThat(result).isEmpty()
    }
}
