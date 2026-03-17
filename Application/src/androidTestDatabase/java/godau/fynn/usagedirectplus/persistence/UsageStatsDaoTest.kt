package godau.fynn.usagedirectplus.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.SimpleUsageStat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.runner.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class UsageStatsDaoTest {

    private lateinit var db: HistoryDatabase
    private lateinit var dao: UsageStatsDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.getUsageStatsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndQueryTotalTime() {
        dao.insert(listOf(
            SimpleUsageStat(1L, 5000L, "com.a", false),
            SimpleUsageStat(1L, 3000L, "com.b", false)
        ))

        assertThat(dao.getTotalTimeUsed()).isEqualTo(8000L)
    }

    @Test
    fun insertAndQueryTotalTimeForDay() {
        dao.insert(listOf(
            SimpleUsageStat(1L, 5000L, "com.a", false),
            SimpleUsageStat(2L, 3000L, "com.b", false)
        ))

        assertThat(dao.getTotalTimeUsed(1L)).isEqualTo(5000L)
        assertThat(dao.getTotalTimeUsed(2L)).isEqualTo(3000L)
    }

    @Test
    fun hiddenStatsExcludedFromTotalTime() {
        dao.insert(listOf(
            SimpleUsageStat(1L, 5000L, "com.a", false),
            SimpleUsageStat(1L, 3000L, "com.b", true)
        ))

        assertThat(dao.getTotalTimeUsed()).isEqualTo(5000L)
    }

    @Test
    fun getDaysStoredReturnsDistinctDays() {
        dao.insert(listOf(
            SimpleUsageStat(1L, 100L, "com.a", false),
            SimpleUsageStat(1L, 200L, "com.b", false),
            SimpleUsageStat(3L, 300L, "com.c", false)
        ))

        val days = dao.getDaysStored()
        assertThat(days.toList()).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun getDaysStoredExcludesHidden() {
        dao.insert(listOf(
            SimpleUsageStat(1L, 100L, "com.a", false),
            SimpleUsageStat(2L, 200L, "com.b", true)
        ))

        val days = dao.getDaysStored()
        assertThat(days.toList()).containsExactly(1L)
    }

    @Test
    fun insertIncrementalAddsToExisting() {
        dao.insert(listOf(SimpleUsageStat(1L, 1000L, "com.a", false)))

        dao.insertIncremental(listOf(SimpleUsageStat(1L, 500L, "com.a")))

        assertThat(dao.getTotalTimeUsed(1L)).isEqualTo(1500L)
    }

    @Test
    fun insertIncrementalPreservesHiddenFlag() {
        dao.insert(listOf(SimpleUsageStat(1L, 1000L, "com.a", true)))

        dao.insertIncremental(listOf(SimpleUsageStat(1L, 500L, "com.a")))

        // Should still be hidden
        assertThat(dao.getHiddenAmount()).isEqualTo(1)
    }

    @Test
    fun insertIncrementalNewApp() {
        dao.insert(listOf(SimpleUsageStat(1L, 1000L, "com.a", false)))

        dao.insertIncremental(listOf(SimpleUsageStat(1L, 500L, "com.b")))

        assertThat(dao.getTotalTimeUsed(1L)).isEqualTo(1500L)
    }

    @Test
    fun markHiddenSetsFlag() {
        val stat = SimpleUsageStat(1L, 1000L, "com.a", false)
        dao.insert(listOf(stat))

        dao.markHidden(stat)

        assertThat(dao.getHiddenAmount()).isEqualTo(1)
        assertThat(dao.getTotalTimeUsed()).isEqualTo(0L)
    }

    @Test
    fun markUnhiddenAllClearsAllHidden() {
        dao.insert(listOf(
            SimpleUsageStat(1L, 1000L, "com.a", true),
            SimpleUsageStat(1L, 2000L, "com.b", true)
        ))

        dao.markUnhiddenAll()

        assertThat(dao.getHiddenAmount()).isEqualTo(0)
        assertThat(dao.getTotalTimeUsed()).isEqualTo(3000L)
    }
}
