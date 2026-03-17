package godau.fynn.usagedirectplus.view.adapter

import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.view.adapter.TimelineViewPagerAdapter.Companion.toTimelineSegment
import godau.fynn.usagedirectplus.wrapper.ComponentForegroundStat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TimelineSegmentConversionTest {

    private fun millisForTime(hour: Int, minute: Int, second: Int = 0): Long {
        return LocalDate.now()
            .atTime(LocalTime.of(hour, minute, second))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun `converts timestamps to correct minute of day`() {
        val stat = ComponentForegroundStat(
            millisForTime(14, 30, 0),
            millisForTime(15, 0, 0),
            "com.example"
        )
        val colorMap = mapOf("com.example" to Color.RED)

        val segment = stat.toTimelineSegment(colorMap, Color.GRAY)

        assertThat(segment.startMinuteOfDay).isEqualTo(14 * 60f + 30f)
        assertThat(segment.endMinuteOfDay).isEqualTo(15 * 60f)
        assertThat(segment.color).isEqualTo(Color.RED)
    }

    @Test
    fun `uses default color when package not in map`() {
        val stat = ComponentForegroundStat(
            millisForTime(10, 0),
            millisForTime(10, 30),
            "com.unknown"
        )
        val colorMap = mapOf("com.other" to Color.BLUE)

        val segment = stat.toTimelineSegment(colorMap, Color.GRAY)

        assertThat(segment.color).isEqualTo(Color.GRAY)
    }

    @Test
    fun `midnight start produces minute 0`() {
        val stat = ComponentForegroundStat(
            millisForTime(0, 0, 0),
            millisForTime(0, 5, 0),
            "com.example"
        )

        val segment = stat.toTimelineSegment(emptyMap(), Color.GRAY)

        assertThat(segment.startMinuteOfDay).isEqualTo(0f)
        assertThat(segment.endMinuteOfDay).isEqualTo(5f)
    }

    @Test
    fun `late evening produces correct minutes`() {
        val stat = ComponentForegroundStat(
            millisForTime(23, 45, 0),
            millisForTime(23, 59, 30),
            "com.example"
        )

        val segment = stat.toTimelineSegment(emptyMap(), Color.GRAY)

        assertThat(segment.startMinuteOfDay).isEqualTo(23 * 60f + 45f)
        // 23:59:30 = 23*60 + 59 + 30/60 = 1439.5
        assertThat(segment.endMinuteOfDay).isEqualTo(23 * 60f + 59f + 30f / 60f)
    }

    @Test
    fun `seconds contribute fractional minutes`() {
        val stat = ComponentForegroundStat(
            millisForTime(12, 0, 30),
            millisForTime(12, 1, 0),
            "com.example"
        )

        val segment = stat.toTimelineSegment(emptyMap(), Color.GRAY)

        // 12:00:30 = 720 + 0 + 0.5 = 720.5
        assertThat(segment.startMinuteOfDay).isEqualTo(720.5f)
        assertThat(segment.endMinuteOfDay).isEqualTo(721f)
    }
}
