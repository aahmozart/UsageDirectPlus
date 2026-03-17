package godau.fynn.usagedirectplus.wrapper

import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.SimpleUsageStat
import org.junit.jupiter.api.Test

class UsageStatsManagerWrapperTest {

    @Test
    fun `aggregateSimpleUsageStats sums timeUsed`() {
        val stats = listOf(
            SimpleUsageStat(1L, 100L, "com.a"),
            SimpleUsageStat(1L, 200L, "com.b"),
            SimpleUsageStat(1L, 300L, "com.c")
        )

        assertThat(UsageStatsManagerWrapper.aggregateSimpleUsageStats(stats)).isEqualTo(600L)
    }

    @Test
    fun `aggregateSimpleUsageStats returns zero for empty list`() {
        assertThat(UsageStatsManagerWrapper.aggregateSimpleUsageStats(emptyList())).isEqualTo(0L)
    }

    @Test
    fun `aggregateSimpleUsageStats single item returns its timeUsed`() {
        val stats = listOf(SimpleUsageStat(1L, 42L, "com.single"))

        assertThat(UsageStatsManagerWrapper.aggregateSimpleUsageStats(stats)).isEqualTo(42L)
    }
}
