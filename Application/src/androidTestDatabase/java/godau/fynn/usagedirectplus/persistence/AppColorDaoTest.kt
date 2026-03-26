package godau.fynn.usagedirectplus.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.persistence.combined.TimeAppColor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppColorDaoTest {

    private lateinit var db: HistoryDatabase
    private lateinit var appColorDao: AppColorDao
    private lateinit var usageStatsDao: UsageStatsDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()
        appColorDao = db.getAppColorDao()
        usageStatsDao = db.getUsageStatsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun updateExclusiveStoresColorsByApplicationId() {
        appColorDao.updateExclusive(
            arrayOf(
                AppColor("com.a", 0x112233, 2),
                AppColor("com.b", 0x445566, 1)
            )
        )

        assertThat(appColorDao.getAppColor("com.a")).isEqualTo(AppColor("com.a", 0x112233, 2))
        assertThat(appColorDao.getAppColor("com.b")).isEqualTo(AppColor("com.b", 0x445566, 1))
        assertThat(appColorDao.getAppColorMap()).containsExactly(
            "com.a", 0x112233,
            "com.b", 0x445566
        )
    }

    @Test
    fun deleteRemovesColorResolvedThroughAppsTable() {
        val appColor = AppColor("com.a", 0x112233, 2)
        appColorDao.updateExclusive(arrayOf(appColor))

        appColorDao.delete(appColor)

        assertThat(appColorDao.getAppColor("com.a")).isNull()
        assertThat(appColorDao.getAppColorMap()).isEmpty()
    }

    @Test
    fun getTimeAppColorsKeepsUncoloredAppsVisible() {
        usageStatsDao.insert(
            listOf(
                SimpleUsageStat(1L, 5_000L, "com.a", false),
                SimpleUsageStat(1L, 3_000L, "com.b", false)
            )
        )
        appColorDao.updateExclusive(arrayOf(AppColor("com.a", 0x112233, 2)))

        val result = appColorDao.getTimeAppColors()

        assertThat(result.map(TimeAppColor::applicationId)).containsExactly("com.a", "com.b").inOrder()
        assertThat(result[0].appColor).isEqualTo(AppColor("com.a", 0x112233, 2))
        assertThat(result[1].appColor).isNull()
    }
}
