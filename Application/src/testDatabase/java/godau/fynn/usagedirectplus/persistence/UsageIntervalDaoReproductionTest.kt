package godau.fynn.usagedirectplus.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UsageIntervalDaoReproductionTest {

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

    /**
     * Bug 1 (parallel): Confirms insertNonOverlapping correctly deduplicates
     * when the same intervals are re-inserted (unlike insertIncremental).
     */
    @Test
    fun insertNonOverlappingDoesNotDuplicate_whenReprocessingSameTimeRange() {
        val intervals = listOf(UsageInterval(1000L, 301_000L, "com.a"))

        dao.insertNonOverlapping(intervals)
        // Same intervals re-inserted (simulating job re-run)
        dao.insertNonOverlapping(intervals)

        val result = dao.getByApp("com.a")
        assertThat(result).hasSize(1)
        assertThat(result[0].endTime - result[0].beginTime).isEqualTo(300_000L)
    }

    /**
     * Bug 2: Zero-duration intervals (beginTime == endTime) are accepted and stored.
     * These waste storage and can affect statistics. They should be filtered out.
     */
    @Test
    fun insertNonOverlapping_acceptsZeroDurationIntervals() {
        dao.insertNonOverlapping(listOf(
            UsageInterval(1000L, 1000L, "com.a"),  // zero-duration
            UsageInterval(2000L, 3000L, "com.a")   // normal
        ))

        val result = dao.getByApp("com.a")
        // BUG: returns 2 rows - the zero-duration interval is stored
        // Expected: only 1 row (the valid interval)
        assertThat(result).hasSize(1)
        assertThat(result[0].beginTime).isEqualTo(2000L)
    }

    /**
     * Bug 3: getByTimeRange uses `beginTime >= :start AND endTime <= :end` which
     * only returns intervals fully contained within the range. Intervals that cross
     * midnight boundaries are missed by both days' queries.
     */
    @Test
    fun getByTimeRange_missesMidnightCrossingInterval() {
        // Interval that starts at "23:56" of day 1 and ends at "00:03" of day 2
        // Day 1 range: 0 - 86_400_000, Day 2 range: 86_400_000 - 172_800_000
        val crossMidnight = UsageInterval(86_160_000L, 86_580_000L, "com.a") // 23:56 -> 00:03

        dao.insertNonOverlapping(listOf(crossMidnight))

        // Query for day 1: should find it (it overlaps day 1)
        val day1Result = dao.getByTimeRange(0L, 86_400_000L)
        // BUG: returns empty - the interval's endTime > day1 end
        assertThat(day1Result).hasSize(1)

        // Query for day 2: should also find it (it overlaps day 2)
        val day2Result = dao.getByTimeRange(86_400_000L, 172_800_000L)
        // BUG: returns empty - the interval's beginTime < day2 start
        assertThat(day2Result).hasSize(1)
    }

    /**
     * Bug 3 (variant): Same issue with getByAppAndTimeRange.
     */
    @Test
    fun getByAppAndTimeRange_missesMidnightCrossingInterval() {
        val crossMidnight = UsageInterval(86_160_000L, 86_580_000L, "com.a")
        dao.insertNonOverlapping(listOf(crossMidnight))

        val result = dao.getByAppAndTimeRange("com.a", 0L, 86_400_000L)
        // BUG: returns empty
        assertThat(result).hasSize(1)
    }
}
