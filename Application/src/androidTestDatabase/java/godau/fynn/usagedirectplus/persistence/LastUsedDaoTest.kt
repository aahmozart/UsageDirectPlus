package godau.fynn.usagedirectplus.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LastUsedDaoTest {

    private lateinit var db: HistoryDatabase
    private lateinit var dao: LastUsedDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.getLastUsedDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertResolvesAppIdsAndReturnsStringKeys() {
        dao.insert(
            mapOf(
                "com.a" to 100L,
                "com.b" to 200L
            )
        )

        assertThat(dao.getLastUsedStats().toList()).containsExactly(
            LastUsedStat("com.a", 100L),
            LastUsedStat("com.b", 200L)
        )
    }
}
