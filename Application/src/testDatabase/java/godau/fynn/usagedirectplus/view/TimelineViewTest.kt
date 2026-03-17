package godau.fynn.usagedirectplus.view

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TimelineViewTest {

    @Test
    fun `minuteToX at minute 0 returns chartLeft`() {
        val x = TimelineView.minuteToX(0f, 16f, 300f)
        assertThat(x).isEqualTo(16f)
    }

    @Test
    fun `minuteToX at minute 1440 returns chartLeft plus chartWidth`() {
        val x = TimelineView.minuteToX(1440f, 16f, 300f)
        assertThat(x).isEqualTo(316f)
    }

    @Test
    fun `minuteToX at minute 720 returns midpoint`() {
        val x = TimelineView.minuteToX(720f, 16f, 300f)
        assertThat(x).isEqualTo(166f)
    }

    @Test
    fun `minuteToX at noon with zero padding`() {
        val x = TimelineView.minuteToX(720f, 0f, 1440f)
        assertThat(x).isEqualTo(720f)
    }

    @Test
    fun `minuteToX at 6am`() {
        val x = TimelineView.minuteToX(360f, 10f, 480f)
        // 10 + (360/1440) * 480 = 10 + 0.25 * 480 = 10 + 120 = 130
        assertThat(x).isEqualTo(130f)
    }
}
