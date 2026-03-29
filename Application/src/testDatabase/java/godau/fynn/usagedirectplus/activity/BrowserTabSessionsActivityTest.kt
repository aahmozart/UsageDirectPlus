package godau.fynn.usagedirectplus.activity

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.browser.BrowserSupport
import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.thread.icon.IconThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class BrowserTabSessionsActivityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(HistoryDatabase.DATABASE_NAME)
        context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        IconThread.nameMap[APPLICATION_ID] = "Vanadium"
        IconThread.iconMap.remove(APPLICATION_ID)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(HistoryDatabase.DATABASE_NAME)
        context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        IconThread.nameMap.remove(APPLICATION_ID)
        IconThread.iconMap.remove(APPLICATION_ID)
    }

    @Test
    fun showsCapturedSessionsWhenPresentOnFirstLaunch() {
        insertClosedSession(hostname = "reddit.com")

        val controller = Robolectric.buildActivity(BrowserTabSessionsActivity::class.java, createIntent())
            .setup()
        val activity = controller.get()

        waitForCondition {
            recyclerView(activity).adapter?.itemCount == 1 &&
                recyclerView(activity).visibility == View.VISIBLE
        }

        assertThat(emptyMessage(activity).visibility).isEqualTo(View.GONE)
        assertThat(summaryText(activity).visibility).isEqualTo(View.VISIBLE)
        assertThat(summaryText(activity).text.toString()).contains("1 captured tabs")

        controller.pause().stop().destroy()
    }

    @Test
    fun reloadsSessionsWhenReturningToForeground() {
        val controller = Robolectric.buildActivity(BrowserTabSessionsActivity::class.java, createIntent())
            .setup()
        val activity = controller.get()

        waitForCondition { emptyMessage(activity).visibility == View.VISIBLE }
        assertThat(recyclerView(activity).adapter).isNull()

        controller.pause().stop()

        insertClosedSession(hostname = "example.com")

        controller.restart().start().resume().visible()

        waitForCondition {
            recyclerView(activity).adapter?.itemCount == 1 &&
                emptyMessage(activity).visibility == View.GONE
        }

        assertThat(summaryText(activity).visibility).isEqualTo(View.VISIBLE)
        assertThat(enableCaptureButton(activity).visibility).isEqualTo(View.GONE)

        controller.pause().stop().destroy()
    }

    private fun insertClosedSession(hostname: String) {
        val date = LocalDate.ofEpochDay(DAY)
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val openedAt = start + TimeUnit.MINUTES.toMillis(1)
        val closedAt = openedAt + TimeUnit.MINUTES.toMillis(5)

        runBlocking {
            withContext(Dispatchers.IO) {
                val database = HistoryDatabase.get(context)
                try {
                    val dao = database.getBrowserTabSessionDao()
                    val id = dao.insertOpenSession(
                        applicationId = APPLICATION_ID,
                        openedAt = openedAt,
                        hostname = hostname,
                        privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                        urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                    )
                    dao.closeOpenSession(
                        id = id,
                        closedAt = closedAt,
                        closeReason = BrowserTabSession.CLOSE_REASON_APP_BACKGROUND,
                        hostname = hostname,
                        privacyMode = BrowserTabSession.PRIVACY_MODE_UNKNOWN,
                        urlConfidence = BrowserTabSession.URL_CONFIDENCE_HIGH
                    )
                } finally {
                    database.close()
                }
            }
        }
    }

    private fun createIntent(): Intent {
        return Intent(context, BrowserTabSessionsActivity::class.java).apply {
            putExtra("applicationId", APPLICATION_ID)
            putExtra("day", DAY)
        }
    }

    private fun waitForCondition(condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3)
        while (System.nanoTime() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            Thread.sleep(10)
        }
        throw AssertionError("Condition not met before timeout")
    }

    private fun recyclerView(activity: BrowserTabSessionsActivity): RecyclerView {
        return activity.findViewById(R.id.recyclerview)
    }

    private fun emptyMessage(activity: BrowserTabSessionsActivity) =
        activity.findViewById<android.widget.TextView>(R.id.empty_message)

    private fun summaryText(activity: BrowserTabSessionsActivity) =
        activity.findViewById<android.widget.TextView>(R.id.summary_text)

    private fun enableCaptureButton(activity: BrowserTabSessionsActivity) =
        activity.findViewById<android.widget.Button>(R.id.enable_capture_button)

    companion object {
        private const val APPLICATION_ID = BrowserSupport.VANADIUM_PACKAGE
        private val DAY = LocalDate.of(2026, 3, 27).toEpochDay()
    }
}
