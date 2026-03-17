package godau.fynn.usagedirectplus.wrapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HarmonizedEventLogWrapperTest {

    @Test
    fun `empty list returns empty`() {
        val result = HarmonizedEventLogWrapper.harmonize(emptyList())

        assertThat(result).isEmpty()
    }

    @Test
    fun `single element returns same element`() {
        val event = ComponentForegroundStat(1000L, 2000L, "com.a")

        val result = HarmonizedEventLogWrapper.harmonize(listOf(event))

        assertThat(result).hasSize(1)
        assertThat(result[0].beginTime).isEqualTo(1000L)
        assertThat(result[0].endTime).isEqualTo(2000L)
        assertThat(result[0].packageName).isEqualTo("com.a")
    }

    @Test
    fun `merges same-package events within harmony interval`() {
        // Gap of 10s (< 30s HARMONY_INTERVAL) between events of same package
        val events = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(12000L, 15000L, "com.a")  // 10s gap
        )

        val result = HarmonizedEventLogWrapper.harmonize(events)

        assertThat(result).hasSize(1)
        assertThat(result[0].beginTime).isEqualTo(1000L)
        assertThat(result[0].endTime).isEqualTo(15000L)
    }

    @Test
    fun `does not merge same-package events outside harmony interval`() {
        // Gap of 31s (> 30s HARMONY_INTERVAL) between events of same package
        val events = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(33000L, 40000L, "com.a")  // 31s gap
        )

        val result = HarmonizedEventLogWrapper.harmonize(events)

        assertThat(result).hasSize(2)
    }

    @Test
    fun `does not merge different packages`() {
        val events = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(2500L, 4000L, "com.b")  // different package, small gap
        )

        val result = HarmonizedEventLogWrapper.harmonize(events)

        assertThat(result).hasSize(2)
        assertThat(result[0].packageName).isEqualTo("com.a")
        assertThat(result[1].packageName).isEqualTo("com.b")
    }

    @Test
    fun `harmonizes start time of adjacent different-package events`() {
        // Different packages with small gap: start time of second event should be harmonized
        val events = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(2500L, 5000L, "com.b")  // 500ms gap < 30s
        )

        val result = HarmonizedEventLogWrapper.harmonize(events)

        assertThat(result).hasSize(2)
        assertThat(result[0].endTime).isEqualTo(2000L)
        // Second event's beginTime harmonized to first event's endTime
        assertThat(result[1].beginTime).isEqualTo(2000L)
        assertThat(result[1].endTime).isEqualTo(5000L)
    }

    @Test
    fun `does not harmonize start time of distant different-package events`() {
        val events = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(60000L, 70000L, "com.b")  // 58s gap > 30s
        )

        val result = HarmonizedEventLogWrapper.harmonize(events)

        assertThat(result).hasSize(2)
        assertThat(result[1].beginTime).isEqualTo(60000L)  // unchanged
    }

    @Test
    fun `merges chain of same-package events`() {
        // Three events of same package, each within harmony interval of the previous
        val events = listOf(
            ComponentForegroundStat(1000L, 2000L, "com.a"),
            ComponentForegroundStat(5000L, 6000L, "com.a"),   // 3s gap
            ComponentForegroundStat(10000L, 12000L, "com.a")  // 4s gap from merged end
        )

        val result = HarmonizedEventLogWrapper.harmonize(events)

        assertThat(result).hasSize(1)
        assertThat(result[0].beginTime).isEqualTo(1000L)
        assertThat(result[0].endTime).isEqualTo(12000L)
    }
}
