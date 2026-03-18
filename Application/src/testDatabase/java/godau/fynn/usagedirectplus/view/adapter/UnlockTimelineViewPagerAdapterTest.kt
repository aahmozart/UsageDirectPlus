package godau.fynn.usagedirectplus.view.adapter

import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.persistence.ScreenEvent
import godau.fynn.usagedirectplus.view.adapter.UnlockTimelineViewPagerAdapter.Companion.toUnlockSegments
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class UnlockTimelineViewPagerAdapterTest {

    private val accent = Color.BLUE

    private fun millisForTime(hour: Int, minute: Int, second: Int = 0): Long {
        return LocalDate.now()
            .atTime(LocalTime.of(hour, minute, second))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun `empty events returns empty segments`() {
        val segments = toUnlockSegments(emptyList(), accent)
        assertThat(segments).isEmpty()
    }

    @Test
    fun `single unlock and lock pair produces one segment`() {
        val events = listOf(
            ScreenEvent(millisForTime(9, 0), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(9, 30), ScreenEvent.KEYGUARD_SHOWN)
        )

        val segments = toUnlockSegments(events, accent)

        assertThat(segments).hasSize(1)
        assertThat(segments[0].startMinuteOfDay).isEqualTo(9 * 60f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(9 * 60f + 30f)
        assertThat(segments[0].color).isEqualTo(accent)
    }

    @Test
    fun `day starts unlocked when first event is lock`() {
        val events = listOf(
            ScreenEvent(millisForTime(7, 15), ScreenEvent.KEYGUARD_SHOWN)
        )

        val segments = toUnlockSegments(events, accent)

        assertThat(segments).hasSize(1)
        assertThat(segments[0].startMinuteOfDay).isEqualTo(0f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(7 * 60f + 15f)
    }

    @Test
    fun `day ends unlocked when last event is unlock`() {
        val events = listOf(
            ScreenEvent(millisForTime(22, 0), ScreenEvent.KEYGUARD_HIDDEN)
        )

        val segments = toUnlockSegments(events, accent)

        assertThat(segments).hasSize(1)
        assertThat(segments[0].startMinuteOfDay).isEqualTo(22 * 60f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(1440f)
    }

    @Test
    fun `multiple unlock and lock cycles produce correct segments`() {
        val events = listOf(
            ScreenEvent(millisForTime(8, 0), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(8, 30), ScreenEvent.KEYGUARD_SHOWN),
            ScreenEvent(millisForTime(12, 0), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(12, 45), ScreenEvent.KEYGUARD_SHOWN),
            ScreenEvent(millisForTime(18, 0), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(18, 15), ScreenEvent.KEYGUARD_SHOWN)
        )

        val segments = toUnlockSegments(events, accent)

        assertThat(segments).hasSize(3)
        assertThat(segments[0].startMinuteOfDay).isEqualTo(8 * 60f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(8 * 60f + 30f)
        assertThat(segments[1].startMinuteOfDay).isEqualTo(12 * 60f)
        assertThat(segments[1].endMinuteOfDay).isEqualTo(12 * 60f + 45f)
        assertThat(segments[2].startMinuteOfDay).isEqualTo(18 * 60f)
        assertThat(segments[2].endMinuteOfDay).isEqualTo(18 * 60f + 15f)
    }

    @Test
    fun `consecutive unlocks use first unlock as start`() {
        val events = listOf(
            ScreenEvent(millisForTime(10, 0), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(10, 30), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(11, 0), ScreenEvent.KEYGUARD_SHOWN)
        )

        val segments = toUnlockSegments(events, accent)

        assertThat(segments).hasSize(1)
        assertThat(segments[0].startMinuteOfDay).isEqualTo(10 * 60f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(11 * 60f)
    }

    @Test
    fun `consecutive locks treat first as unlocked since midnight`() {
        val events = listOf(
            ScreenEvent(millisForTime(6, 0), ScreenEvent.KEYGUARD_SHOWN),
            ScreenEvent(millisForTime(7, 0), ScreenEvent.KEYGUARD_SHOWN)
        )

        val segments = toUnlockSegments(events, accent)

        // First lock → segment from 0 to 6:00
        // Second lock → no open unlock period, so no new segment
        assertThat(segments).hasSize(1)
        assertThat(segments[0].startMinuteOfDay).isEqualTo(0f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(6 * 60f)
    }

    @Test
    fun `starts unlocked and ends unlocked`() {
        val events = listOf(
            ScreenEvent(millisForTime(3, 0), ScreenEvent.KEYGUARD_SHOWN),
            ScreenEvent(millisForTime(8, 0), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(9, 0), ScreenEvent.KEYGUARD_SHOWN),
            ScreenEvent(millisForTime(20, 0), ScreenEvent.KEYGUARD_HIDDEN)
        )

        val segments = toUnlockSegments(events, accent)

        assertThat(segments).hasSize(3)
        // Unlocked since midnight until 3:00
        assertThat(segments[0].startMinuteOfDay).isEqualTo(0f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(3 * 60f)
        // 8:00 to 9:00
        assertThat(segments[1].startMinuteOfDay).isEqualTo(8 * 60f)
        assertThat(segments[1].endMinuteOfDay).isEqualTo(9 * 60f)
        // 20:00 to end of day
        assertThat(segments[2].startMinuteOfDay).isEqualTo(20 * 60f)
        assertThat(segments[2].endMinuteOfDay).isEqualTo(1440f)
    }

    @Test
    fun `seconds contribute fractional minutes`() {
        val events = listOf(
            ScreenEvent(millisForTime(12, 0, 30), ScreenEvent.KEYGUARD_HIDDEN),
            ScreenEvent(millisForTime(12, 1, 0), ScreenEvent.KEYGUARD_SHOWN)
        )

        val segments = toUnlockSegments(events, accent)

        assertThat(segments).hasSize(1)
        assertThat(segments[0].startMinuteOfDay).isEqualTo(720.5f)
        assertThat(segments[0].endMinuteOfDay).isEqualTo(721f)
    }
}
