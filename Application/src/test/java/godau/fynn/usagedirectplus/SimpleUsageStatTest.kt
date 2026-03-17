package godau.fynn.usagedirectplus

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SimpleUsageStatTest {

    @Test
    fun `primary constructor sets all fields`() {
        val stat = SimpleUsageStat(10L, 5000L, "com.example", true)

        assertThat(stat.day).isEqualTo(10L)
        assertThat(stat.timeUsed).isEqualTo(5000L)
        assertThat(stat.applicationId).isEqualTo("com.example")
        assertThat(stat.hidden).isTrue()
    }

    @Test
    fun `three-arg constructor defaults hidden to false`() {
        val stat = SimpleUsageStat(10L, 5000L, "com.example")

        assertThat(stat.hidden).isFalse()
    }

    @Test
    fun `three-arg constructor sets day, timeUsed, applicationId`() {
        val stat = SimpleUsageStat(42L, 9999L, "org.test")

        assertThat(stat.day).isEqualTo(42L)
        assertThat(stat.timeUsed).isEqualTo(9999L)
        assertThat(stat.applicationId).isEqualTo("org.test")
    }
}
