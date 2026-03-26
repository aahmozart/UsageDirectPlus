package godau.fynn.usagedirectplus.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.SimpleUsageStat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UsageStatsDaoReproductionTest {

    private lateinit var db: HistoryDatabase
    private lateinit var statsDao: UsageStatsDao
    private lateinit var intervalDao: UsageIntervalDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()
        statsDao = db.getUsageStatsDao()
        intervalDao = db.getUsageIntervalDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * Bug 1: insertIncremental double-counts when re-processing the same time range.
     * This test documents the bug — insertIncremental is no longer used for partial days.
     */
    @Test
    fun insertIncrementalDoubleCounts_whenReprocessingSameTimeRange() {
        // Simulate first run: full day insert (REPLACE) with 5 minutes of usage
        statsDao.insert(listOf(SimpleUsageStat(1L, 300_000L, "com.a", false)))

        // Simulate second run: partial day re-processes the same 5 minutes
        statsDao.insertIncremental(listOf(SimpleUsageStat(1L, 300_000L, "com.a")))

        // BUG: This returns 600_000 (10 min) — documenting the bug in insertIncremental
        assertThat(statsDao.getTotalTimeUsed(1L)).isEqualTo(600_000L)
    }

    /**
     * Fix verification: replaceFromIntervals is idempotent — calling it multiple times
     * with the same intervals produces the same result.
     */
    @Test
    fun replaceFromIntervals_isIdempotent() {
        val intervals = listOf(
            UsageInterval(1000L, 301_000L, "com.a"),  // 5 min
            UsageInterval(400_000L, 520_000L, "com.b") // 2 min
        )

        // Insert intervals and compute stats
        intervalDao.insertNonOverlapping(intervals)
        statsDao.replaceFromIntervals(1L, intervalDao.getByTimeRange(0L, 86_400_000L))

        assertThat(statsDao.getTotalTimeUsed(1L)).isEqualTo(420_000L)

        // Re-insert same intervals (simulating job re-run) and recompute
        intervalDao.insertNonOverlapping(intervals)
        statsDao.replaceFromIntervals(1L, intervalDao.getByTimeRange(0L, 86_400_000L))

        // Should still be the same — no inflation
        assertThat(statsDao.getTotalTimeUsed(1L)).isEqualTo(420_000L)
    }

    /**
     * Fix verification: replaceFromIntervals preserves hidden flags from existing stats.
     */
    @Test
    fun replaceFromIntervals_preservesHiddenFlag() {
        // Mark an app as hidden
        statsDao.insert(listOf(SimpleUsageStat(1L, 100_000L, "com.a", true)))
        assertThat(statsDao.getHiddenAmount()).isEqualTo(1)

        // Recompute from intervals — hidden flag should be preserved
        val intervals = listOf(UsageInterval(1000L, 201_000L, "com.a"))
        intervalDao.insertNonOverlapping(intervals)
        statsDao.replaceFromIntervals(1L, intervalDao.getByTimeRange(0L, 86_400_000L))

        assertThat(statsDao.getHiddenAmount()).isEqualTo(1)
        // Time should come from intervals, not the old stat
        assertThat(statsDao.getTotalTimeUsed(1L)).isEqualTo(0L) // hidden is excluded from total
    }

    /**
     * Fix verification: simulates the full corrected EventLogRunnable flow.
     * First run processes full day, second run processes partial day — total stays correct.
     */
    @Test
    fun correctedFlow_partialDayReprocessingDoesNotInflate() {
        // First run: full day with 5 min of com.a usage
        val firstRunIntervals = listOf(UsageInterval(1000L, 301_000L, "com.a"))
        intervalDao.insertNonOverlapping(firstRunIntervals)
        statsDao.replaceFromIntervals(1L, intervalDao.getByTimeRange(0L, 86_400_000L))

        assertThat(statsDao.getTotalTimeUsed(1L)).isEqualTo(300_000L)

        // Second run: partial day adds 2 more minutes of com.a, plus 1 min of com.b
        val secondRunIntervals = listOf(
            UsageInterval(400_000L, 520_000L, "com.a"),
            UsageInterval(600_000L, 660_000L, "com.b")
        )
        intervalDao.insertNonOverlapping(secondRunIntervals)
        statsDao.replaceFromIntervals(1L, intervalDao.getByTimeRange(0L, 86_400_000L))

        // Total should be 300K + 120K + 60K = 480K (not inflated)
        assertThat(statsDao.getTotalTimeUsed(1L)).isEqualTo(480_000L)
    }
}
