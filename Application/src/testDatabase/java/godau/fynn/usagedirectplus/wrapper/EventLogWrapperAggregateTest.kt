package godau.fynn.usagedirectplus.wrapper

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

class EventLogWrapperAggregateTest {

    private lateinit var wrapper: EventLogWrapper

    @BeforeEach
    fun setUp() {
        val context = mockk<Context>(relaxed = true)
        every { context.getSystemService("usagestats") } returns mockk<UsageStatsManager>(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns mockk<SharedPreferences>(relaxed = true)
        wrapper = EventLogWrapper(context)
    }

    @Test
    fun `aggregateForegroundStats returns empty for empty input`() {
        val result = wrapper.aggregateForegroundStats(emptyList())

        assertThat(result).isEmpty()
    }

    @Test
    fun `aggregateForegroundStats sums time for same package`() {
        val stats = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),  // 1000ms
            ComponentForegroundStat(3000L, 5000L, "com.a")   // 2000ms
        )

        val result = wrapper.aggregateForegroundStats(stats)

        assertThat(result).hasSize(1)
        assertThat(result[0].applicationId).isEqualTo("com.a")
        assertThat(result[0].timeUsed).isEqualTo(3000L)
    }

    @Test
    fun `aggregateForegroundStats separates different packages`() {
        val stats = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(2000L, 4000L, "com.b")
        )

        val result = wrapper.aggregateForegroundStats(stats)

        assertThat(result).hasSize(2)
        val resultMap = result.associateBy { it.applicationId }
        assertThat(resultMap["com.a"]!!.timeUsed).isEqualTo(1000L)
        assertThat(resultMap["com.b"]!!.timeUsed).isEqualTo(2000L)
    }

    @Test
    fun `aggregateForegroundStats calculates correct day from epoch`() {
        // Use a known timestamp: 2024-01-15 12:00:00 UTC
        val timestamp = 1705320000000L
        val expectedDay = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()

        val stats = listOf(
            ComponentForegroundStat(timestamp, timestamp + 1000L, "com.a")
        )

        val result = wrapper.aggregateForegroundStats(stats)

        assertThat(result[0].day).isEqualTo(expectedDay)
    }

    @Test
    fun `aggregateForegroundStats passes end times to endConsumer`() {
        val consumer = LastUsedConsumer()
        val stats = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(3000L, 5000L, "com.a"),
            ComponentForegroundStat(1000L, 3000L, "com.b")
        )

        wrapper.aggregateForegroundStats(stats, consumer)

        // LastUsedConsumer keeps only the last value per app
        assertThat(consumer.applicationLastUsedMap["com.a"]).isEqualTo(5000L)
        assertThat(consumer.applicationLastUsedMap["com.b"]).isEqualTo(3000L)
    }
}
