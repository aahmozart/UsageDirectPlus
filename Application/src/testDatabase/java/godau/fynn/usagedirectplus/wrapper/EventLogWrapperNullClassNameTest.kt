package godau.fynn.usagedirectplus.wrapper

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EventLogWrapperNullClassNameTest {

    private lateinit var mockContext: Context
    private lateinit var mockUsageStatsManager: UsageStatsManager

    @BeforeEach
    fun setUp() {
        mockContext = mockk<Context>(relaxed = true)
        mockUsageStatsManager = mockk<UsageStatsManager>(relaxed = true)
        every { mockContext.getSystemService("usagestats") } returns mockUsageStatsManager
        every { mockContext.getSharedPreferences(any(), any()) } returns mockk<SharedPreferences>(relaxed = true)

        val mockActivityManager = mockk<ActivityManager>()
        every { mockContext.getSystemService(Context.ACTIVITY_SERVICE) } returns mockActivityManager
        every { mockActivityManager.runningAppProcesses } returns emptyList()

        every { mockContext.packageManager } returns mockk<PackageManager>(relaxed = true)

        mockkConstructor(UsageEvents.Event::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(UsageEvents.Event::class)
    }

    private data class EventData(
        val packageName: String,
        val className: String?,
        val eventType: Int,
        val timeStamp: Long
    )

    private fun mockUsageEvents(eventDataList: List<EventData>): UsageEvents {
        val mockEvents = mockk<UsageEvents>()
        var index = 0
        var currentData: EventData? = null

        val hasNextResults = List(eventDataList.size) { true } + false
        every { mockEvents.hasNextEvent() } returnsMany hasNextResults
        every { mockEvents.getNextEvent(any()) } answers {
            currentData = eventDataList[index++]
            true
        }

        every { anyConstructed<UsageEvents.Event>().packageName } answers { currentData!!.packageName }
        every { anyConstructed<UsageEvents.Event>().className } answers { currentData!!.className }
        every { anyConstructed<UsageEvents.Event>().eventType } answers { currentData!!.eventType }
        every { anyConstructed<UsageEvents.Event>().timeStamp } answers { currentData!!.timeStamp }

        return mockEvents
    }

    @Test
    fun `events with null className do not crash`() {
        val events = mockUsageEvents(listOf(
            EventData("com.example.app", "com.example.app.MainActivity",
                UsageEvents.Event.ACTIVITY_RESUMED, 1000L),
            EventData("android", null,
                UsageEvents.Event.DEVICE_SHUTDOWN, 2000L),
            EventData("com.example.app", "com.example.app.MainActivity",
                UsageEvents.Event.ACTIVITY_RESUMED, 3000L)
        ))

        every { mockUsageStatsManager.queryEvents(any(), any()) } returns events

        val wrapper = EventLogWrapper(mockContext)
        // Should not throw NullPointerException
        val result = wrapper.getForegroundStatsByTimestamps(0L, 5000L)

        assertThat(result).isNotNull()
    }

    @Test
    fun `activity events with null className are skipped`() {
        val events = mockUsageEvents(listOf(
            // ACTIVITY_RESUMED with null className — should be skipped
            EventData("com.example.bad", null,
                UsageEvents.Event.ACTIVITY_RESUMED, 1000L),
            // Valid ACTIVITY_RESUMED
            EventData("com.example.good", "com.example.good.MainActivity",
                UsageEvents.Event.ACTIVITY_RESUMED, 2000L),
            // Valid ACTIVITY_PAUSED
            EventData("com.example.good", "com.example.good.MainActivity",
                UsageEvents.Event.ACTIVITY_PAUSED, 4000L)
        ))

        every { mockUsageStatsManager.queryEvents(any(), any()) } returns events

        val wrapper = EventLogWrapper(mockContext)
        val result = wrapper.getForegroundStatsByTimestamps(0L, 5000L)

        // Only the valid event pair should produce a result
        assertThat(result).hasSize(1)
        assertThat(result[0].packageName).isEqualTo("com.example.good")
        assertThat(result[0].beginTime).isEqualTo(2000L)
        assertThat(result[0].endTime).isEqualTo(4000L)
    }

    @Test
    fun `valid events still produce correct results`() {
        val events = mockUsageEvents(listOf(
            EventData("com.example.app", "com.example.app.MainActivity",
                UsageEvents.Event.ACTIVITY_RESUMED, 1000L),
            EventData("com.example.app", "com.example.app.MainActivity",
                UsageEvents.Event.ACTIVITY_PAUSED, 3000L)
        ))

        every { mockUsageStatsManager.queryEvents(any(), any()) } returns events

        val wrapper = EventLogWrapper(mockContext)
        val result = wrapper.getForegroundStatsByTimestamps(0L, 5000L)

        assertThat(result).hasSize(1)
        assertThat(result[0].packageName).isEqualTo("com.example.app")
        assertThat(result[0].beginTime).isEqualTo(1000L)
        assertThat(result[0].endTime).isEqualTo(3000L)
    }
}
