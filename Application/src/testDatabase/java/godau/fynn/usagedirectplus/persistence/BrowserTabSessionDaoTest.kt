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
class BrowserTabSessionDaoTest {

    private lateinit var db: HistoryDatabase
    private lateinit var dao: BrowserTabSessionDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HistoryDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.getBrowserTabSessionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertOpenSessionStoresAndReturnsJoinedOpenSession() {
        val id = dao.insertOpenSession(
            applicationId = "com.android.chrome",
            openedAt = 1_000L,
            title = "Example Domain",
            url = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        val openSession = dao.getOpenSession()

        assertThat(id).isGreaterThan(0L)
        assertThat(openSession).isEqualTo(
            BrowserTabSession(
                id = id,
                openedAt = 1_000L,
                closedAt = null,
                applicationId = "com.android.chrome",
                title = "Example Domain",
                url = "example.com",
                privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH,
                closeReason = null
            )
        )
    }

    @Test
    fun closeOpenSessionKeepsHistoryQueryableByOverlap() {
        val id = dao.insertOpenSession(
            applicationId = "org.mozilla.firefox",
            openedAt = 10_000L,
            title = "Mozilla",
            url = "mozilla.org",
            privacyMode = BrowserTabSession.PRIVACY_MODE_PRIVATE,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        dao.closeOpenSession(
            id = id,
            closedAt = 20_000L,
            closeReason = BrowserTabSession.CLOSE_REASON_APP_BACKGROUND,
            title = "Mozilla",
            url = "mozilla.org",
            privacyMode = BrowserTabSession.PRIVACY_MODE_PRIVATE,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        val result = dao.getByAppAndTimeRange("org.mozilla.firefox", 15_000L, 30_000L)

        assertThat(result).hasSize(1)
        assertThat(result.first().closeReason).isEqualTo(BrowserTabSession.CLOSE_REASON_APP_BACKGROUND)
        assertThat(dao.getOpenSession()).isNull()
    }

    @Test
    fun updateOpenSessionMetadataOverwritesTitleAndUrl() {
        val id = dao.insertOpenSession(
            applicationId = "com.android.chrome",
            openedAt = 1_000L,
            title = "Chrome",
            url = null,
            privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_NONE
        )

        dao.updateOpenSessionMetadata(
            id = id,
            title = "Example Domain",
            url = "example.com",
            privacyMode = BrowserTabSession.PRIVACY_MODE_PRIVATE,
            urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
        )

        val openSession = dao.getOpenSession()
        assertThat(openSession?.title).isEqualTo("Example Domain")
        assertThat(openSession?.url).isEqualTo("example.com")
        assertThat(openSession?.privacyMode).isEqualTo(BrowserTabSession.PRIVACY_MODE_PRIVATE)
    }
}
